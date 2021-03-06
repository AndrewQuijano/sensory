//
//  NSDictionary.swift
//  Poké Prism
//
//  Created by William Falcon on 7/22/16.
//  Copyright © 2016 William Falcon. All rights reserved.
//

import UIKit


public extension NSDictionary {
    
    class func _jsonFromFileName(_ name:String) -> AnyObject? {

        let masterDataUrl: URL = (Bundle.main.url(forResource: name, withExtension: "json")! as NSURL) as URL
        
        do {
            let jsonData: Data = try Data(contentsOf: masterDataUrl as URL)
            let object = try JSONSerialization.jsonObject(with: jsonData as Data, options: .allowFragments)
            return object as AnyObject?
        } catch {
            // Handle Error
            print(error)
            return nil
        }
    }
    
    func _printJSON() {
        do {
            let json = try JSONSerialization.data(withJSONObject: self, options: JSONSerialization.WritingOptions(rawValue: 0))
            let theJSONText = NSString(data: json,
                                       encoding: String.Encoding.ascii.rawValue)
            print("JSON string = \(theJSONText!)")
        } catch {
            print(error)
        }
    }
}
