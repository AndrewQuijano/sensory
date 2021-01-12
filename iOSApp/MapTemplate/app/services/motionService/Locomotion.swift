//
//  Locomotion.swift
//  911
//
//  Created by William Falcon on 8/23/16.
//  Copyright © 2016 William Falcon. All rights reserved.
//

import UIKit
import CoreMotion

//get history for last 24 hours
let MOTION_HISTORY_TIMEFRAME_FOR_TRAINING_MINUTES = 60 * 24;
let MOTION_HISTORY_TIMEFRAME_FOR_PREDICTION_MINUTES = 30;

class Locomotion: NSObject {
    
    static let sharedInstance = Locomotion()
    var motionManager: CMMotionManager?
    var altimeter: CMAltimeter?
    var onMotion: CMAccelerometerHandler?
    var onMag: CMMagnetometerHandler?
    
    override fileprivate init() {
        super.init()
        motionManager = CMMotionManager()
        altimeter = CMAltimeter()
    }
    
    class func streamAccelerometerUpdates(_ onMotion: @escaping CMAccelerometerHandler) {
        
//        print("HIIIIIIIIIIII")
//        print("stream accelerometer")
        let motionManager = Locomotion.sharedInstance.motionManager!
//        print(motionManager.magnetometerData)
        motionManager.magnetometerUpdateInterval = 1.0
        motionManager.startMagnetometerUpdates()
//        print("magneettttts")
//        print(motionManager.magnetometerData)
        motionManager.accelerometerUpdateInterval = 1.0
        motionManager.startAccelerometerUpdates(to: OperationQueue.main, withHandler: onMotion)

//        motionManager.startAccelerometerUpdates(to: OperationQueue.main) { (data, error) in
//
//                    //turn into dict
//                    if let lastLoc = self.lastLocation {
//                        let datapoint = self.generateDatapointFromLocation(lastLoc)
//
//                        //track json DP ONLY if have barometer pressure
//                        if let jsonDP = self.generateJSONDatapointForLocation(lastLoc) {
//                            self.jsonDataPoints.addObject(jsonDP)
//                            self.pointCountLabel.text = "DPs: \(self.jsonDataPoints.count)"
//
//                            if (self.jsonDataPoints.count % 5 == 0) {
//                                //reload to show point
//                                self.tableView.reloadData()
//                            }
//                        }
//
//                        //track last point for view
//                        self.dataPoints.addObject(datapoint)
//
//                    }
//                }
    }
    // Virgil notes: just copied this function, bc had to do with statusBarManager https://stackoverflow.com/questions/59464159/ios-13-get-wifi-and-cellular-network-signal-strength
    class func signalStrength() -> Double {
        if #available(iOS 13.0, *) {
            if let statusBarManager = UIApplication.shared.keyWindow?.windowScene?.statusBarManager,
                let localStatusBar = statusBarManager.value(forKey: "createLocalStatusBar") as? NSObject,
                let statusBar = localStatusBar.value(forKey: "statusBar") as? NSObject,
                let _statusBar = statusBar.value(forKey: "_statusBar") as? UIView,
                let currentData = _statusBar.value(forKey: "currentData")  as? NSObject,
                let celluar = currentData.value(forKey: "cellularEntry") as? NSObject,
                let signalStrength = celluar.value(forKey: "displayValue") as? Int {
                return Double(signalStrength)
            } else {
                return 0
            }
        } else {
            var signalStrength = -1
            let application = UIApplication.shared
            let statusBarView = application.value(forKey: "statusBar") as! UIView
            let foregroundView = statusBarView.value(forKey: "foregroundView") as! UIView
            let foregroundViewSubviews = foregroundView.subviews
            var dataNetworkItemView: UIView!
            for subview in foregroundViewSubviews {
                if subview.isKind(of: NSClassFromString("UIStatusBarSignalStrengthItemView")!) {
                    dataNetworkItemView = subview
                    break
                } else {
                    signalStrength = -1
                }
            }
            signalStrength = dataNetworkItemView.value(forKey: "signalStrengthBars") as! Int
            if signalStrength == -1 {
                return 0
            } else {
                return Double(signalStrength)
            }
        }
//        return 0
    }
    
    class func streamBarometerData(_ onData: @escaping CMAltitudeHandler) {
//        print("CHECK STREAM BAROMETER")
        print(CMAltimeter.isRelativeAltitudeAvailable())
        if CMAltimeter.isRelativeAltitudeAvailable() {
            let altimeter = Locomotion.sharedInstance.altimeter!
//            print("streamBarometer")
//            print(altimeter)
            altimeter.startRelativeAltitudeUpdates(to: OperationQueue.main, withHandler: onData)
            
            
        }
    }
    
    class func stopStreamBarometerData() {
        
        if CMAltimeter.isRelativeAltitudeAvailable() {
            let altimeter = Locomotion.sharedInstance.altimeter!
            altimeter.stopRelativeAltitudeUpdates()
            
        }
    }
    class func streamMagnetometerUpdates(_ onMag: @escaping CMMagnetometerHandler) {
            let motionManager = Locomotion.sharedInstance.motionManager!
    //        print(motionManager.magnetometerData)
            motionManager.magnetometerUpdateInterval = 1.0
        motionManager.startMagnetometerUpdates(to: OperationQueue.main, withHandler: onMag)
    }
}
