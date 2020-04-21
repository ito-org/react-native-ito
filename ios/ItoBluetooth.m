#import <React/RCTBridgeModule.h>
#import <ItoBluetooth-Swift.h>
#import "ItoBluetooth-Bridging-Header.h"

@interface RCT_EXTERN_MODULE(ItoBluetooth, NSObject)

 RCT_EXPORT_METHOD(callback:(RCTResponseSenderBlock)onDistanceMeasurements)
 {
     SwItoBluetooth *sbt = [SwItoBluetooth new] ;
     onDistanceMeasurements(sbt.onDistanceMeasurements);
 }

RCT_EXPORT_METHOD(publishBeaconUUIDs:(NSNumber *) from:(NSNumber*) to: callback:(RCTResponseSenderBlock)onSuccess){
     SwItoBluetooth *sbt = [SwItoBluetooth new] ;
    if( [sbt publishBeaconUUIDsFrom: from to: to]) {
        onSuccess;
    }
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(isPossiblyInfected){
    SwItoBluetooth *sbt = [SwItoBluetooth new] ;
    
    NSNumber *retval = [sbt isPossiblyInfected];
    return retval;
}

RCT_EXPORT_METHOD(restartTracingService){
    SwItoBluetooth *sbt = [SwItoBluetooth new];
    [sbt restartTracingService];
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(getLatestFetchTime){
    SwItoBluetooth *sbt = [SwItoBluetooth new] ;
    NSNumber *retval = [sbt getLatestFetchTime];
    return retval;
}
@end
