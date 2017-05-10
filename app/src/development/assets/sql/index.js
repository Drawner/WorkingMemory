// to debug
require('@google-cloud/debug-agent').start({ allowExpressions: true });

var functions = require('firebase-functions');

const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

// // Start writing Firebase Functions
// // https://firebase.google.com/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// })

const root = admin.database();




// Move any 'updates' from one device to the user's other devices.
exports.sync = functions.database.ref('/sync/{user}/{device}/OUT/{record}').onWrite(function(event){

  const value = event.data.val();
  
  const usrDevices = root.ref('/devices/'+event.params.user);

  return usrDevices.once('value').then(function(snapshot){
  
      usrDevices.off('value');
  
  	  var promises = [];
	  
	  promises.push(Promise.resolve());

	  var syncOp;
			
      snapshot.forEach(function(childSnapshot){

        var device = childSnapshot.key;

        // If not the same device.
        if (device != event.params.device) {

           // Copy the record into this device's IN directory.
           syncOp = root.ref('/sync/'+event.params.user+'/'+device+'/IN').push().set(value);
		   
		   promises.push(syncOp);
        }
		
		return false;
      });

      // Delete the original record.
      syncOp = root.ref('/sync/'+event.params.user+'/'+event.params.device+'/OUT/'+event.params.record).remove(function (error){

          if (error) {

          }else{

          }
      });

	  promises.push(syncOp);
	  
	  return Promise.all(promises);
    });
});



// Trim the size of the database by removing inactive records.
exports.trimdatabase = functions.database.ref('/devices/{userid}/{deviceid}').onWrite(function(event){

   // Only continue if this is a new device record just being created.
   // Saves on processing time and avoids infinite loops when deleting devices.
   if (event.data.previous.exists()){

      return;
   }
/*   
   const usrDevices = root.ref('/devices');
   
   return usrDevices.once('value').then(function(devices){
   
      usrDevices.off('value');

	  return trimDB(devices).then(function(values) { 

	     return newDevice(event.params.userid, event.params.deviceid);
	  });
   });
*/
   return thruDevices(trimDB).then(function(value){ 

         // With every new device, check to see if the user already has records.
	     return newDevice(event.params.userid, event.params.deviceid);
	});
});



function thruDevices(func...funcs){

   // The devices table reference
   const usrDevices = root.ref('/devices');
   
   return usrDevices.once('value').then(function(devices){
   
       usrDevices.off('value');
	   
	   var promises = [];
	  
	   promises.push(Promise.resolve(func(devices)));
	   
	   for(let nextFunc of funcs){
	   
          promises.push(nextFunc(devices));
	   }
	  return Promise.all(promises);
   });	   
}


function trimDB(devices){

      // Parent Reference.
      var parentRef = devices.ref.parent;

      // Today. Right now in days.
      const nowIndays = Math.floor((new Date).getTime()/1000/86400);

      var promises = [];
	  
	  promises.push(Promise.resolve());
  
      var syncOp;
  
      // Go through all the users and their devices.
      devices.forEach(function(users){
	  
		 var deviceCount = 0;
		 
		 // Unique identifier representing a user.
	     var userid = users.key;

		 // Go through this user's devices.
         users.forEach(function(deviceRec){
		 
		    // Last time this device was updated (in days)
			var stampIndays = Math.floor(deviceRec.val()/86400);
		 
		    // If less than a year since the device was last used ignore.
            if(nowIndays - stampIndays < 365){

               deviceCount++;
			   
			   // This is in a function so don't use continue.
		       return false;
            }
			
			// Delete that device.
            syncOp = parentRef(userid + '/' + deviceRec.key).remove().then(function(result){
			
			            // Delete any syc records under that device.
			            return root.ref('/sync/' + userid + '/' + deviceRec.key).remove();
			        });
			
			promises.push(syncOp);
			
			// Continue to iterate.
			return false;
		});
		
		// The user is still using devices.
		if(deviceCount > 0){

           // Return true would terminate the iteration.
		   return false;
		}
		
		// Delete the user
		// Remove the user's devices
		// Remove the user's tasks
		// Remove the user's sync records if any.
		syncOp = parentRef.child(userid).remove().then(function(result){
		
		            return root.ref('/tasks/' + userid).remove().then(function(result){
	
    	               return root.ref('/sync/' + userid).remove();
		            });
		        });
		
		promises.push(syncOp);
		
		// Continue to iterate.
        return false;
      });
	  
	  return Promise.all(promises);
}




function newDevice(userid, deviceid){

      // The user may already have tasks
      const tasksRef = root.ref('/tasks/' + userid);
  
	  return tasksRef.once('value').then(function(userTasks){
	  
	     tasksRef.off('value');
		 
		 // Must sync those tasks with this new device.
		 // Reference the directory to contain the new records.
         const syncRef = root.ref('/sync/' + userid + '/' + deviceid + '/IN');
		 
	     return syncNewDevice(userTasks, syncRef);
	  });
};




function syncNewDevice(userTasks, syncRef){
   
   var promises = [];
	  
   promises.push(Promise.resolve());

   // Today. Right now.
   const stamp = Math.floor((new Date).getTime()/1000);
	  
   // Create an sync record object;
   var record = {action:'UPDATE'};

   // Go through all of this user's tasks and put them into this new device.
   userTasks.forEach(function(tasksRec){
	  
	  // Unique task record identifier.
      record.key = tasksRec.key;
       
      record.timestamp = stamp; 

      // Insert the record.
      // Push all these promisese into an array
      promises.push(syncRef.push().set(record));

      // True would terminate iteration.
     return false;
   });
   
   // Have all these finish writing before returning.
   return Promise.all(promises);
}