//
//  BiometricDetector.swift
//
//  Created by Andre Grillo on 24/07/2024.
//

import LocalAuthentication

@objc(BiometricsChange)
class BiometricChangeDetector: CDVPlugin {
    private let context = LAContext()
    private let storedDomainStateKey = "StoredBiometricDomainState"

    @objc(checkForBiometricChanges:)
    func checkForBiometricChanges(_ command: CDVInvokedUrlCommand) {
        var error: NSError?
        let canEvaluatePolicy = context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error)
        
        if !canEvaluatePolicy {
            print("ℹ️ bioNotEnabled")
            return
        }

        guard let newDomainState = context.evaluatedPolicyDomainState else {
            print("ℹ️ bioNotEnabled")
            return
        }
        
        let storedDomainState = KeychainHelper.getDomainState(forKey: storedDomainStateKey)
        
        if storedDomainState == nil {
            KeychainHelper.saveDomainState(newDomainState, forKey: storedDomainStateKey)
            print("ℹ️ firstBioCheck")
        } else {
            if let storedDomainState = storedDomainState, storedDomainState == newDomainState {
                print("ℹ️ sameBio")
            } else {
                KeychainHelper.saveDomainState(newDomainState, forKey: storedDomainStateKey)
                print("ℹ️ bioReset")
            }
        }
    }
}
