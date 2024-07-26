var exec = require('cordova/exec');

exports.biometricsChanged = function (success, error) {
    exec(success, error, 'BiometricChangeDetector', 'checkForBiometricChanges', []);
};
