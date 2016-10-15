# cordova-plugin-filemanager

This plugin allows you to easily build a file explorer.

## Methods

- __hasPermission__: Verify whether app has external storage permission.

```js
FileManager.hasPermission(function () {
  // success
}, function (error) {
  // request for permission
});
```

- __requestPermission__: Request for external storage permission.

```js
FileManager.requestPermission(function () {
  // success
}, function (error) {
  // permission denied
});
```

- __getDirectoryListing__: Get list of directories and files at given path.

```js
FileManager.getDirectoryListing(function (result) {
  // success
}, function (error) {
  // failed to retrieve list
}[, path, filter]);
```

## Constants

- __ERROR_UNKNOWN__
- __ERROR_FILE_NOT_FOUND__
- __ERROR_PERMISSION_DENIED__
