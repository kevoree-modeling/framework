/**
 * Created by cyril on 17/02/15.
 */
'use strict';

var html5rocks = {};
html5rocks.indexedDB = {};
html5rocks.indexedDB.version = 1;
html5rocks.indexedDB.chanSignal = "signal";
html5rocks.indexedDB.chanAnnounce = "announce";
html5rocks.indexedDB.db = null;
html5rocks.indexedDB.open = function() {
    var request = indexedDB.open("signaling", html5rocks.indexedDB.version);

    request.onupgradeneeded = function(e) {
        var db = e.target.result;

        e.target.transaction.onerror = html5rocks.indexedDB.onerror;

        if( !db.objectStoreNames.contains( html5rocks.indexedDB.chanSignal ) ) {
            db.createObjectStore( html5rocks.indexedDB.chanSignal,  {keyPath: "timestamp"});
        }
        if( !db.objectStoreNames.contains( html5rocks.indexedDB.chanAnnounce ) ) {
            db.createObjectStore( html5rocks.indexedDB.chanAnnounce,  {keyPath: "id"});
        }
        console.log("DB is upgraded!");
    };
    request.onsuccess = function(e) {
        html5rocks.indexedDB.db = e.target.result;
        console.log("DB is open!");
    };
    request.onerror = function(e) {
        console.log("An error occurred");
        console.dir(e);
    };
    request.onblocked = function(e) {
        console.log("Blocked");
    };
};
html5rocks.indexedDB.sendAnnounce = function( id, sharedKey ) {
    var request = indexedDB.open("signaling", html5rocks.indexedDB.version);
    request.onsuccess = function(e) {
        html5rocks.indexedDB.db = e.target.result;
        var transaction = html5rocks.indexedDB.db.transaction([html5rocks.indexedDB.chanAnnounce], "readwrite");
        var store = transaction.objectStore( html5rocks.indexedDB.chanAnnounce );
        var keyIndex = store.createIndex("by_sharedKey", "sharedKey", {unique: true});
        var request = store.put({
            "id": id,
            "sharedKey" : sharedKey
        });
        transaction.oncomplete = function(e) {
            console.log('Announced our sharedKey is ' + sharedKey);
            console.log('Announced our ID is ' + id);
            console.log("Send announce transaction complete");
        };
    };
    request.onerror = function(e) {
        console.log(e.value);
    };
};
html5rocks.indexedDB.sendSignal = function( sharedKey, message ) {
    var request = indexedDB.open("signaling", html5rocks.indexedDB.version);
    request.onsuccess = function(e) {
        html5rocks.indexedDB.db = e.target.result;
        var transaction = html5rocks.indexedDB.db.transaction([html5rocks.indexedDB.chanSignal], "readwrite");
        var store = transaction.objectStore( html5rocks.indexedDB.chanSignal );
        var keyIndex = store.createIndex("by_sharedKey", "sharedKey", {unique: true});
        var request = store.put({
            "data": message,
            "sharedKey" : sharedKey,
            "timeStamp" : new Date().getTime()
        });
        transaction.oncomplete = function(e) {
            console.log('Signal message: ' + message);
            console.log("Transaction complete");
        };
    };
    request.onerror = function(e) {
        console.log(e.value);
    };
};
html5rocks.indexedDB.getAnnounce = function( sharedKey ) {
    var request = indexedDB.open("signaling", html5rocks.indexedDB.version);
    request.onsuccess = function(e) {
        html5rocks.indexedDB.db = e.target.result;
        var transaction = html5rocks.indexedDB.db.transaction([html5rocks.indexedDB.chanAnnounce], "readonly");
        var store = transaction.objectStore( html5rocks.indexedDB.chanAnnounce );
        var index = store.index("by_sharedKey");

        var request = index.get( sharedKey );
        console.log('GET announced our sharedKey is ' + sharedKey);

        var matching = request.result;
        if (matching !== undefined) {
            // A match was found.
            return matching;
        } else {
            // No match was found.
            return null;
        }
    };
    request.onerror = function(e) {
        console.log(e.value);
        return null;
    };
};
html5rocks.indexedDB.getSignal = function( sharedKey ) {
    var request = indexedDB.open("signaling", html5rocks.indexedDB.version);
    request.onsuccess = function(e) {
        html5rocks.indexedDB.db = e.target.result;
        var transaction = html5rocks.indexedDB.db.transaction([html5rocks.indexedDB.chanSignal], "readonly");
        var store = transaction.objectStore( html5rocks.indexedDB.chanSignal );
        var index = store.index("by_sharedKey");

        var request = index.get( sharedKey );
        console.log('GET announced our sharedKey is ' + sharedKey);

        var matching = request.result;
        if (matching !== undefined) {
            // A match was found.
            return matching;
        } else {
            // No match was found.
            return null;
        }
    };
    request.onerror = function(e) {
        console.log(e.value);
        return null;
    };
};
