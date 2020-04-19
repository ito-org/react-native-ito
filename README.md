# ito library for React Native apps

![License](https://img.shields.io/github/license/ito-org/react-native-ito-bluetooth)

## What it does

This library is the native backend for the ito app. It does several things
- generate TCNs (Temporary Contact Numbers) as per the STRICT protocol
- handle bluetooth communication
- store the data received over bluetooth
- store the data used to generate the TCNs
- regularly poll the server for TCN reports and compare with the local database
- upload TCN generation data from specified timeframes
- provide some feedback for better UX

## Getting started

`$ npm install react-native-ito-bluetooth --save`

### Mostly automatic installation

`$ react-native link react-native-ito-bluetooth`

## Usage
```javascript
import {NativeModules, NativeEventEmitter} from 'react-native';

// start tracing (after getting location permission for Bluetooth advertising and scanning)
NativeModules.ItoBluetooth.restartTracing();

const eventEmitter = new NativeEventEmitter(NativeModules.ItoBluetooth);
this.eventListener = eventEmitter.addListener('onDistancesChanged', (distances) => {
  //get notified about the distances to nearby devices
});

// whether the user is likely to have been in contact with an infected user
NativeModules.ItoBluetooth.isPossiblyInfected()

// upload the user's own TCNs of the last 7 days
const nowSeconds = Date.now() / 1000;
const sevenDaysAgo = nowSeconds - 7 * 24 * 60 * 60;
NativeModules.ItoBluetooth.publishBeaconUUIDs(
  sevenDaysAgo,
  now,
  (success) => {
    console.log('upload ' + success ? 'succeeded' : 'failed');
  },
)
```
