//
//  BiometricDetector.swift
//
//  Created by Andre Grillo on 24/07/2024.
//

import LocalAuthentication

@objc(BiometricsChange)
class BiometricChangeDetector: CDVPlugin {
    @objc(checkForBiometricChanges:)
    func checkForBiometricChanges(_ command: CDVInvokedUrlCommand) {
        var error: NSError?
        let context = LAContext()
        let storedDomainStateKey = "StoredBiometricDomainState"
        let canEvaluatePolicy = context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error)
        
        if !canEvaluatePolicy {
            print("ℹ️ bioNotAvailable")
            sendPluginResult(status: CDVCommandStatus_OK, message: "BioNotAvailable", callbackId: command.callbackId)
            return
        }

        guard let newDomainState = context.evaluatedPolicyDomainState else {
            print("ℹ️ bioNotEnabled")
            sendPluginResult(status: CDVCommandStatus_OK, message: "BioNotEnabled", callbackId: command.callbackId)
            return
        }
        
        let storedDomainState = KeychainHelper.getDomainState(forKey: storedDomainStateKey)
        
        if storedDomainState == nil {
            KeychainHelper.saveDomainState(newDomainState, forKey: storedDomainStateKey)
            print("ℹ️ firstBioCheck")
            sendPluginResult(status: CDVCommandStatus_OK, message: "FirstBioCheck", callbackId: command.callbackId)
        } else {
            if let storedDomainState = storedDomainState, storedDomainState == newDomainState {
                print("ℹ️ sameBio")
                sendPluginResult(status: CDVCommandStatus_OK, message: "SameBio", callbackId: command.callbackId)
            } else {
                KeychainHelper.saveDomainState(newDomainState, forKey: storedDomainStateKey)
                print("ℹ️ bioReset")
                sendPluginResult(status: CDVCommandStatus_OK, message: "BioReset", callbackId: command.callbackId)
            }
        }
    }
    
    func sendPluginResult(status: CDVCommandStatus, message: String = "", callbackId: String, keepCallback: Bool = false) {
        let pluginResult = CDVPluginResult(status: status, messageAs: message)
        pluginResult?.setKeepCallbackAs(keepCallback)
        self.commandDelegate!.send(pluginResult, callbackId: callbackId)
    }
}
