# ito library for React Native apps

[![License](https://img.shields.io/badge/license-BSD--3--Clause--Clear-brightgreen)](LICENSE)

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
Retrieve the native module.
```js
import {NativeModules, NativeEventEmitter} from 'react-native';

const { ItoBluetooth } = NativeModules;
```

Start tracing (after getting location permission for Bluetooth advertising and scanning):
```javascript
ItoBluetooth.restartTracing();
```
Get the current and recent distances of ito devices in the vicinity.
```js
const eventEmitter = new NativeEventEmitter(ItoBluetooth);
this.eventListener = eventEmitter.addListener('onDistancesChanged', (distances) => {
  //get notified about the distances to nearby devices
});
```
Find out whether the user is likely to have been in contact with an infected user
```js
ItoBluetooth.isPossiblyInfected() // boolean
```

Upload the user's own TCNs of the last 7 days
```js
const nowSeconds = Date.now() / 1000;
const sevenDaysAgo = nowSeconds - 7 * 24 * 60 * 60;
ItoBluetooth.publishBeaconUUIDs(
  sevenDaysAgo,
  now,
  (success) => {
    console.log('upload ' + success ? 'succeeded' : 'failed');
  },
)
```
