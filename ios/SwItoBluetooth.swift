//
//  SwItoBluetooth.swift
//  
//
//  Created by Dieder Timmers on 18/04/2020.
//

import Foundation
@objc public class SwItoBluetooth :NSObject {
    
    @objc func publishBeaconUUIDs(from: NSNumber, to: NSNumber) -> Bool{
        return false
    }
    
    @objc func onDistanceMeasurements() -> NSArray{
        return []
    }
    
    @objc func isPossiblyInfected() -> NSNumber{
        return true
    }
    
    @objc func restartTracingService(){
        
    }
    
    @objc func getLatestFetchTime() -> NSNumber{
        return 0
    }
}
