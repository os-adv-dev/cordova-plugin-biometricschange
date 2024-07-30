var exec = require('cordova/exec');

exports.checkForBiometricChanges = function (success, error) {
    exec(success, error, 'BiometricChangeDetector', 'checkForBiometricChanges', []);
};
