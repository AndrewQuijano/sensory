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
  static let motionManager = CMMotionManager()
    var city: String?
    var country: String?
  var avgFloors = ["0-2", "3-5", "6-10", "10-20", "20-50", "50+"]
  var activities = ["walking", "running", "biking", "subway", "driving", "boat", "plane"]
  
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
        print("check datacollector/ Locomotion.streamBarometerData")
        if self.shouldSetBaseAltitude {
          self.baseAltitude = altitudeData
          self.shouldSetBaseAltitude = false
        }
      }
        
      
//        Locomotion.streamMagnetometerUpdates {(magnetometerData, error) in let x = }
      Locomotion.streamAccelerometerUpdates { (accelData, error) in
        let dp = self.generateDatapointFrom(self.lastAltitudeReading, location: self.lastLocationReading, accelData: accelData)
        print("check datacollector/ Locomotion.streamAcellerometerUpdates")
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
          print("data uploaded")
          
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
//    print("check local.DataQueue")
//    print(local.dataQueue)
    
    motionManager.startMagnetometerUpdates()
//    print("datacollector magnets")
//    print("x",motionManager.magnetometerData?.magneticField.x)
//    print("y",motionManager.magnetometerData?.magneticField.y)
//    print("z",motionManager.magnetometerData?.magneticField.z)
    
    local.dataQueue.removeAll()
    
    let url = "\(AppSettings.API_ROOT())/predict"
    let headers = ["Content-Type": "application/json"]
    Alamofire.request(url, method: .post, parameters: ["data": tempData], encoding: JSONEncoding.default, headers: headers)
      .validate()
      .responseJSON { response in
        switch response.result {
        case .success:
//          print("data uploaded")
          
          if let json = response.result.value as? NSDictionary {
            let floor = json["floor"] as? Double
            
            onSuccess(floor ?? 0.0)
          }
            
        case .failure(let error):
          
          onFailure(error as NSError)
        }
    }
    
  }
  
    
  //MARK: - Parsing
  func generateDatapointFrom(_ altitudeData: CMAltitudeData?, location: CLLocation?, accelData: CMAccelerometerData?) -> [String : AnyObject]? {
  
    if let altitudeData = altitudeData, let location = location {
        var dp : [String: AnyObject] = [
        "created_at": Date()._UTCTimestamp() as AnyObject,
        "gps_latitude": location.coordinate.latitude as AnyObject,
        "gps_longitude": location.coordinate.longitude as AnyObject,
        "gps_alt": location.altitude as AnyObject,
        "gps_vertical_accuracy": location.verticalAccuracy as AnyObject,
        "gps_horizontal_accuracy": location.horizontalAccuracy as AnyObject,
        "gps_course": location.course as AnyObject,
        "gps_speed": location.speed as AnyObject,
        "device_id" : UIDevice.current.identifierForVendor!.uuidString as AnyObject,
        "baro_relative_altitude": altitudeData.relativeAltitude.doubleValue as AnyObject,
        "baro_pressure": altitudeData.pressure.doubleValue as AnyObject,
        "magnet_x_mt": DataCollector.motionManager.magnetometerData?.magneticField.x as AnyObject,
        "magnet_y_mt": DataCollector.motionManager.magnetometerData?.magneticField.y as AnyObject,
        "magnet_z_mt": DataCollector.motionManager.magnetometerData?.magneticField.z as AnyObject,
        "magnet_total": DataCollector.motionManager.magnetometerData?.magneticField.x as AnyObject,
            "city_name": "" as AnyObject,
            "country_name": "" as AnyObject,
            // troubled ones, still pending
            "rssi_strength" : Locomotion.signalStrength() as AnyObject,
            "env_context": "" as AnyObject,
            "floor": 0 as AnyObject,
            "env_mean_bldg_floors": self.avgFloors[0] as AnyObject,
            "env_activity": self.activities[0] as AnyObject,
            "indoors": 0 as AnyObject
        ];
//      print(dp)
        let x = dp["magnet_x_mt"] as! Double
        let y = dp["magnet_y_mt"] as! Double
        let z = dp["magnet_z_mt"] as! Double
        let total = sqrt((x*x) + (y*y) + (z*z))
        dp["magnet_total"] = total as AnyObject
        // https://stackoverflow.com/questions/27735835/convert-coordinates-to-city-name/27740680
        let geoCoder = CLGeocoder()
        let location_new = CLLocation(latitude: location.coordinate.latitude, longitude: location.coordinate.longitude)
        geoCoder.reverseGeocodeLocation(location_new, completionHandler:
                  {
                      placemarks, error -> Void in
                      // Place details
                      guard let placeMark = placemarks?.first else { return }
                    self.city = placeMark.subAdministrativeArea
                    self.country = placeMark.country
                      
              })
        
        dp["city_name"] = self.city?.lowercased() as AnyObject
        dp["country_name"] = self.country?.lowercased() as AnyObject
        print(dp)
        return dp
    }
    return nil
  }
}
