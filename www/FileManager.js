var exec = require('cordova/exec'),
    FileManager = {

        ERROR_UNKNOWN: 1,
        ERROR_FILE_NOT_FOUND: 2,
        ERROR_PERMISSION_DENIED: 3,

        hasPermission: function (success, error) {
            exec(success, error, 'FileManager', 'hasPermission', []);
        },

        requestPermission: function (success, error) {
            exec(success, error, 'FileManager', 'requestPermission', []);
        },

        getDirectoryListing: function (success, error, path, filter) {
            exec(success, error, 'FileManager', 'getDirectoryListing', [path, filter]);
        }
    };

module.exports = FileManager;