/* eslint-disable react-native/no-inline-styles */
import { base64Image } from '../base64image';
import * as React from 'react';

import {
  StyleSheet,
  View,
  Text,
  Button,
  SafeAreaView,
  ScrollView,
  StatusBar,
  useColorScheme,
  TouchableOpacity,
  FlatList,
  PermissionsAndroid,
} from 'react-native';
import { Colors, Header } from 'react-native/Libraries/NewAppScreen';
import {
  EscPosImageWithTCPConnectionBitImageWrapper,
  EscPosImageWithTCPConnectionGraphicsImageWrapper,
  EscPosImageWithTCPConnectionRasterBitImageWrapper,
  closePrinterSocket,
  getPairedDevices,
  isAppIgnoringBatteryOptimization,
  openDrawer,
  printBLCut,
  printBLFeed,
  printImageByBluetooth,
  scanBLDevice,
  startNetworkDiscovery,
} from 'sunmi-external-printer';
import { useState } from 'react';
import { convertHTMLtoBase64 } from '../../src';
import type { printerDevice } from 'src/printerDevice';
function App(): JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';
  const [ipAddress, setIpAddress] = useState<string>('');
  const [port, setPort] = useState<string>('');
  const [printerName, setPrinterName] = useState<string>('');
  const [devices, setListofDevices] = useState<ItemData[]>([]);
  const [currPrinter, setCurrPrinter] = useState<printerDevice | null>(null);
  const [blDevices, setListofBlDevices] = useState<printerDevice[]>([]);
  const [showFlatListNetwork, setShowFlatListNetwork] = useState<boolean>(true);
  const [showFlatListBT, setShowFlatListBT] = useState<boolean>(false);
  const Item2 = ({ item, onPress, backgroundColor, textColor }: any) => (
    <TouchableOpacity
      onPress={onPress}
      style={[styles.item, { backgroundColor }]}
    >
      <Text style={[styles.title, { color: textColor }]}>{item.name}</Text>
      <Text style={[styles.title, { color: textColor }]}>{item.address}</Text>
    </TouchableOpacity>
  );
  const renderItem2 = ({ item }: { item: printerDevice }) => {
    const backgroundColor =
      item.name ===
      (currPrinter === null ? ' ' : (currPrinter!!.name as string))
        ? '#00008B'
        : 'blue';
    return (
      <Item2
        item={item}
        onPress={async () => {
          setCurrPrinter(item);
        }}
        backgroundColor={backgroundColor}
        textColor={'white'}
      />
    );
  };
  const renderItem = ({ item }: { item: ItemData }) => {
    const backgroundColor =
      item.printerIPAddress === ipAddress ? '#00008B' : 'blue';
    return (
      <Item
        item={item}
        onPress={async () => {
          setIpAddress(item.printerIPAddress);
          setPort(item.printerPort);
          setPrinterName(item.printerName);
          setListofDevices([]);
        }}
        backgroundColor={backgroundColor}
        textColor={'white'}
      />
    );
  };

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  return (
    <SafeAreaView style={backgroundStyle}>
      <StatusBar
        barStyle={isDarkMode ? 'light-content' : 'dark-content'}
        backgroundColor={backgroundStyle.backgroundColor}
      />
      <ScrollView
        contentInsetAdjustmentBehavior="automatic"
        style={backgroundStyle}
      >
        <Header />
        <View
          style={{
            backgroundColor: isDarkMode ? Colors.black : Colors.white,
          }}
        >
          <Button
            title="Print with Graphic Image Wrapper"
            onPress={async () => {
              const Print =
                await EscPosImageWithTCPConnectionGraphicsImageWrapper(
                  base64Image,
                  ipAddress,
                  port
                );
              console.log(Print);
            }}
          />
          <Button
            title="Print with Bit Wrapper"
            onPress={async () => {
              const Print = await EscPosImageWithTCPConnectionBitImageWrapper(
                base64Image,
                ipAddress,
                port
              );
              console.log(Print);
            }}
          />
          <Button
            title="Print with Raster Bit Wrapper "
            onPress={async () => {
              const Print =
                await EscPosImageWithTCPConnectionRasterBitImageWrapper(
                  base64Image,
                  '192.168.1.1',
                  '9100'
                );
              console.log(Print);
            }}
          />
          <Button
            title="Start Network Discovery"
            onPress={async () => {
              const check = await startNetworkDiscovery();
              console.log(check);
            }}
          />
          <Button
            title="Convert HTMl to Image"
            onPress={async () => {
              const Print = await convertHTMLtoBase64(
                '' +
                  '<html>\n' +
                  '<head>\n' +
                  '<style>\n' +
                  'body {\n' +
                  '  background-color: lightblue;\n' +
                  '}\n' +
                  '\n' +
                  'h1 {\n' +
                  '  text-align: center;\n' +
                  '}\n' +
                  '\n' +
                  'p {\n' +
                  '  font-family: verdana;\n' +
                  '  font-size: 20px;\n' +
                  '}\n' +
                  'p.korean {\n' +
                  '  font-family: Single Day;\n' +
                  '  font-size: 20px;\n' +
                  '}\n' +
                  '</style>\n' +
                  '</head>' +
                  '<body>' +
                  '<h1>Hello, world.</h1>' +
                  '<p>الصفحة الرئيسية \n' + // Arabiac
                  '<br>你好，世界 \n' + // Chinese
                  '<br>こんにちは世界 \n' + // Japanese
                  '<br>Привет мир \n' + // Russian
                  '<br>नमस्ते दुनिया \n' + //  Hindi
                  '<p class="korean"><br>안녕하세요 세계</p>' + // if necessary, you can download and install on your environment the Single Day from fonts.google...
                  '</body>',
                400
              );
              console.log(Print);
            }}
          />
          <Button
            title="Open Drawer"
            onPress={async () => {
              const drawer = await openDrawer(ipAddress, port);
              console.log(drawer);
            }}
          />
          <Button
            title="Is app Ignoring Battery Optimization"
            onPress={async () => {
              const result = await isAppIgnoringBatteryOptimization();
              console.log(result);
            }}
          />
          <Button
            title="startBTDisovery"
            onPress={async () => {
              const requestBLPermissions = async () => {
                const res = await PermissionsAndroid.request(
                  PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION!!
                );
                await PermissionsAndroid.requestMultiple([
                  PermissionsAndroid.PERMISSIONS.BLUETOOTH_SCAN!!,
                  PermissionsAndroid.PERMISSIONS.BLUETOOTH_CONNECT!!,
                ]);
                console.log(res);
              };
              await requestBLPermissions();
              const granted = await PermissionsAndroid.request(
                PermissionsAndroid.PERMISSIONS.BLUETOOTH_SCAN!!,
                {
                  title: 'Android Scan Permission',
                  message: 'Scan Bluetooth Permission',
                  buttonNeutral: 'A ask Me Later',
                  buttonNegative: 'Cancel',
                  buttonPositive: 'OK',
                }
              );
              if (granted) {
                console.log('In granted');
                const results = await scanBLDevice();
                console.log('Passed Results');
                console.log(results);
                console.log(results.filtered_result);
                setListofBlDevices(results.filtered_result);
                setShowFlatListBT(true);
                setShowFlatListNetwork(false);
                setListofDevices([]);
              }
            }}
          />
          <Button
            title="getPairedDevices"
            onPress={async () => {
              const requestBLPermissions = async () => {
                const res = await PermissionsAndroid.request(
                  PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION!!
                );
                await PermissionsAndroid.requestMultiple([
                  PermissionsAndroid.PERMISSIONS.BLUETOOTH_SCAN!!,
                  PermissionsAndroid.PERMISSIONS.BLUETOOTH_CONNECT!!,
                ]);
                console.log(res);
              };
              await requestBLPermissions();
              const granted = await PermissionsAndroid.request(
                PermissionsAndroid.PERMISSIONS.BLUETOOTH_SCAN!!,
                {
                  title: 'Android Scan Permission',
                  message: 'Scan Bluetooth Permission',
                  buttonNeutral: 'A ask Me Later',
                  buttonNegative: 'Cancel',
                  buttonPositive: 'OK',
                }
              );
              if (granted) {
                const results = await getPairedDevices();
                console.log(results);
                setListofBlDevices(results);
                setShowFlatListBT(true);
                setShowFlatListNetwork(false);
                setListofDevices([]);
              }
            }}
          />

          <Button
            title="printImageByBluetooth"
            onPress={async () => {
              console.log('Button Here');
              const result = await printImageByBluetooth(
                currPrinter!!,
                base64Image
              );

              setTimeout(async () => {
                // await 2 seconds to close Socket so that printer cut command is able to be carried out

                await closePrinterSocket();
              }, 2000);

              console.log(result);
            }}
          />
          <Button
            title="cutByBluetooth"
            onPress={async () => {
              console.log('Button Here');
              const result = await printBLCut(currPrinter!!);

              setTimeout(async () => {
                // await 2 seconds to close Socket so that printer cut command is able to be carried out

                await closePrinterSocket();
              }, 2000);

              console.log(result);
            }}
          />
          <Text style={{ alignSelf: 'center', fontSize: 20, marginTop: 10 }}>
            Current IP Printer Device:{printerName}
          </Text>
          <Text style={{ alignSelf: 'center', fontSize: 20, marginTop: 10 }}>
            IPAddress:{ipAddress}
          </Text>
          <Text style={{ alignSelf: 'center', fontSize: 20, marginTop: 10 }}>
            Port:{port}
          </Text>
          <Text style={{ alignSelf: 'center', fontSize: 20, marginTop: 10 }}>
            Current BTPrinter:{currPrinter == null ? ' ' : currPrinter.name}{' '}
            {currPrinter == null ? ' ' : currPrinter.address}
          </Text>
        </View>
      </ScrollView>
      <View style={{ borderWidth: 5, height: 300 }}>
        {showFlatListBT && (
          <FlatList
            data={blDevices}
            renderItem={renderItem2}
            keyExtractor={(item) => item.address}
            extraData={currPrinter}
          />
        )}
        {showFlatListNetwork && (
          <FlatList
            data={devices}
            renderItem={renderItem}
            keyExtractor={(item) => item.printerIPAddress}
            extraData={ipAddress}
          />
        )}
      </View>
    </SafeAreaView>
  );
}
type ItemData = {
  printerName: string;
  printerIPAddress: string;
  printerPort: string;
};

type ItemProps = {
  item: ItemData;
  onPress: () => void;
  backgroundColor: string;
  textColor: string;
};

const Item = ({ item, onPress, backgroundColor, textColor }: ItemProps) => (
  <TouchableOpacity
    onPress={onPress}
    style={[styles.item, { backgroundColor }]}
  >
    <Text style={[styles.title, { color: textColor }]}>
      {item.printerIPAddress}
    </Text>
    <Text style={[styles.title, { color: textColor }]}>{item.printerName}</Text>
    <Text style={[styles.title, { color: textColor }]}>{item.printerPort}</Text>
  </TouchableOpacity>
);
const styles = StyleSheet.create({
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: '600',
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
  },
  highlight: {
    fontWeight: '700',
  },
  item: {
    flex: 1,
    padding: 20,
    marginVertical: 8,
    marginHorizontal: 16,
  },
  title: {
    fontSize: 10,
  },
  devicesContainer: {
    height: '300',
  },
});

export default App;
