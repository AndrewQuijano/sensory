//
//  DataCollector.swift
//  911
//
//  Created by Will Falcon on 8/24/16.
//  Copyright © 2016 William Falcon. All rights reserved.
//

import UIKit
import CoreMotion
import CoreLocation
import Alamofire

class DataCollector: NSObject {
  let uploadThreshold = 60 * 10
  
  static let sharedInstace = DataCollector()
  var lastAltitudeReading: CMAltitudeData?
  var lastLocationReading: CLLocation?
  var dataQueue: [[String : AnyObject]] = []
  var baseAltitude: CMAltitudeData?
  var shouldSetBaseAltitude = false
  
  
  //MARK: - Init
  override fileprivate init() {
    super.init()
    registerForNotifications()
  }
  
  class func setBaseAltitude() {
    DataCollector.sharedInstace.shouldSetBaseAltitude = true
  }
  
  func registerForNotifications() {
    let notificationCenter = NotificationCenter.default
    notificationCenter.addObserver(self, selector: #selector(appMovedToBackground), name: UIApplication.willResignActiveNotification, object: nil)
  }
  
  
  //MARK: - Public methods
  class func startDataCollection() {
    DataCollector.sharedInstace.startDataCollection()
  }
  
  class func getLocalAltitudeEstimate() {
    
  }
  
  func startDataCollection() {
    let status = CLLocationManager.authorizationStatus()
    print("startDataCollection")
    
    if status == .authorizedWhenInUse || status == .authorizedAlways {
      Compass.startMonitoringBackgroundLocation(kCLLocationAccuracyBest) { (location) in
        self.lastLocationReading = location
      }
    print("check 1")
    
    Locomotion.streamBarometerData { (altitudeData, error) in
        self.lastAltitudeReading = altitudeData
        //track base altitude
        print("check 3")
        if self.shouldSetBaseAltitude {
          self.baseAltitude = altitudeData
          self.shouldSetBaseAltitude = false
        }
      }
      
   
      Locomotion.streamAccelerometerUpdates { (accelData, error) in
        let dp = self.generateDatapointFrom(self.lastAltitudeReading, location: self.lastLocationReading, accelData: accelData)
        print("check 4")
        if let dp = dp {
        //print(self.dataQueue)
          self.dataQueue.append(dp)
            //print(self.dataQueue)
            if self.dataQueue.count > self.uploadThreshold {
           // print(self.uploadDataQueue())
            self.uploadDataQueue()
          }
        }
      }
    }
  }
  
  //MARK: - Data Upload
    @objc func appMovedToBackground() {
    print("App moved to background!")
    uploadDataQueue()
  }
  
  func uploadDataQueue(_ onSuccess:(() -> ())? = nil, onFailure:((_ error: NSError)->())? = nil) {
    //clear the dataQ in preparation for uploading
    let tempData = self.dataQueue
    self.dataQueue.removeAll()
    
    let url = "\(AppSettings.API_ROOT())/train"
    let headers = ["Content-Type": "application/json"]

    Alamofire.request(url, method: .post, parameters: ["data": tempData], encoding: JSONEncoding.default, headers: headers)
      .validate()
      .responseJSON { response in
        switch response.result {
        case .success:
          print("data uploaded1")
          
          onSuccess?()
        case .failure(let error):
          print(error)
          print(error.localizedDescription)
          onFailure?(error as NSError)
        }
    }
  }
  
  
  class func predictLocation(_ onSuccess:@escaping ((_ floor: Double) -> ()), onFailure:@escaping ((_ error: NSError) -> ())) {
    
    let local = DataCollector.sharedInstace
//    print("sharedInstace")
//    print(sharedInstace)
//    print("baseAlt")
//    print(sharedInstace.baseAltitude)
//    print("dataque")
//    print(sharedInstace.dataQueue)
//    print("check 5")
//    print("local.lastAltitudeReading")
//    print(local.lastAltitudeReading)
    
    
//        if let altNow = local.lastAltitudeReading, let base = local.baseAltitude {
//
//          let delta = altNow.relativeAltitude.doubleValue - base.relativeAltitude.doubleValue
//
//          var floor = ceil(delta / 3.0)
//          if floor == -0 {
//            floor = 0
//          }
//            onSuccess(floor)
//
//
//        }else {
//            onFailure(NSError(domain: "app", code: 1, userInfo: ["error":"notFound"]))
//        }
    //clear the dataQ in preparation for uploading
    let tempData = local.dataQueue
    print("check 6")
    print(local.dataQueue)
    print(Locomotion.sharedInstance.motionManager?.magnetometerData)
    local.dataQueue.removeAll()
    
    let url = "\(AppSettings.API_ROOT())/predict"
    let headers = ["Content-Type": "application/json"]
    Alamofire.request(url, method: .post, parameters: ["data": tempData], encoding: JSONEncoding.default, headers: headers)
      .validate()
      .responseJSON { response in
        switch response.result {
        case .success:
          print("data uploaded")
          
          if let json = response.result.value as? NSDictionary {
            let floor = json["floor"] as? Double
            
            onSuccess(floor ?? 0.0)
          }
            
        case .failure(let error):
          print("uh oh...")
          print(error)
          print(url)
          print(error.localizedDescription)
          onFailure(error as NSError)
        }
    }
    
  }
  
  
  //MARK: - Parsing
  func generateDatapointFrom(_ altitudeData: CMAltitudeData?, location: CLLocation?, accelData: CMAccelerometerData?) -> [String : AnyObject]? {
    
    if let altitudeData = altitudeData, let location = location {
      
      let dp : [String: AnyObject] = [
        "created_at": Date()._UTCTimestamp() as AnyObject,
        "latitude": location.coordinate.latitude as AnyObject,
        "longitude": location.coordinate.longitude as AnyObject,
        "gps_alt": location.altitude as AnyObject,
        "gps_vertical_accuracy": location.verticalAccuracy as AnyObject,
        "gps_horizontal_accuracy": location.horizontalAccuracy as AnyObject,
        "gps_course": location.course as AnyObject,
        "gps_speed": location.speed as AnyObject,
        "device_id" : UIDevice.current.identifierForVendor!.uuidString as AnyObject,
        "alt": altitudeData.relativeAltitude.doubleValue as AnyObject,
        "alt_pressure": altitudeData.pressure.doubleValue as AnyObject
        
      ];
      
      return dp
    }
    return nil
  }
}
