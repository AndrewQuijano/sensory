//
//  Map+Transitions.swift
//  911
//
//  Created by William Falcon on 8/22/16.
//  Copyright © 2016 William Falcon. All rights reserved.
//

import Foundation

@available(iOS 10.0, *)
extension MapViewController {
  // This is where Map storyboard connects to other storyboards
    // This is where you would add showCollectorVC;
    // Unfortunately it doesn't have simple CollectorViewController to make new instance out of
    // You will have to look into UITabBar, look at first collector storyboard you'll see
    // *don't forget to import UIKit 
  func showAddressbook() {
    let vc = AddressbookViewController._newInstance() as! AddressbookViewController
    addressbookVC = vc
    self.present(vc, animated: true) {
      
    }
  }
  
  func showContactVC() {
    syncAddress()
    let vc = ContactViewController._newInstance() as! ContactViewController
    contactVC = vc
    vc.address = address
    self.present(vc, animated: true) {
      
    }
  }
  
  func syncAddress() {
    address.streetAddress = addressLabel.text
    address.apartment = aptNumLabel.text
    address.floor = floorNumberLabel.text
  }
  
  func showSaveAddressVC() {
    syncAddress()
    let vc = ContactViewController._newInstance() as! ContactViewController
    contactVC = vc
    vc.pageMode = .save

    vc.address = address
    AppDelegate.MOC().insert(address)
    self.present(vc, animated: true) {
      
    }
  }
}
