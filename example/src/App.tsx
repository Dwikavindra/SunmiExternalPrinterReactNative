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
} from 'react-native';
import { Colors, Header } from 'react-native/Libraries/NewAppScreen';
import {
  EscPosImageWithTCPConnectionBitImageWrapper,
  EscPosImageWithTCPConnectionGraphicsImageWrapper,
  EscPosImageWithTCPConnectionRasterBitImageWrapper,
  startNetworkDiscovery,
  stopNetworkDiscovery,
} from 'sunmi-external-printer';
import { DeviceEventEmitter } from 'react-native';
import { useState } from 'react';
import { convertHTMLtoBase64 } from '../../src';
function App(): JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';
  const [ipAddress, setIpAddress] = useState<string>('');
  const [port, setPort] = useState<string>('');
  const [printerName, setPrinterName] = useState<string>('');
  const [devices, setListofDevices] = useState<ItemData[]>([]);
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
          const Print = await stopNetworkDiscovery();
          DeviceEventEmitter.removeAllListeners();
          setListofDevices([]);
          console.log(Print);
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
                  ipAddress,
                  port
                );
              console.log(Print);
            }}
          />
          <Button
            title="Start Network Discovery"
            onPress={async () => {
              const networkDiscovery = await startNetworkDiscovery();
              // DeviceEventEmitter.addListener('OnPrinterFound', (event) => {
              //   const device: ItemData = {
              //     printerName: event.printername,
              //     printerIPAddress: event.ip,
              //     printerPort: event.port,
              //   };

              //   setListofDevices([...devices, device]);
              // });
              console.log(networkDiscovery);
            }}
          />
          <Button
            title="Stop Discovery"
            onPress={async () => {
              const Print = await stopNetworkDiscovery();
              DeviceEventEmitter.removeAllListeners();
              setListofDevices([]);
              console.log(Print);
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
                  '</body>'
              );
              console.log(Print);
            }}
          />
          <Text style={{ alignSelf: 'center', fontSize: 20, marginTop: 10 }}>
            Current Device:{printerName}
          </Text>
          <Text style={{ alignSelf: 'center', fontSize: 20, marginTop: 10 }}>
            IPAddress:{ipAddress}
          </Text>
          <Text style={{ alignSelf: 'center', fontSize: 20, marginTop: 10 }}>
            Port:{port}
          </Text>
          <Text style={{ alignSelf: 'center', fontSize: 20, marginTop: 10 }}>
            Discover Network List
          </Text>
        </View>
      </ScrollView>
      <View style={{ borderWidth: 5, height: 300 }}>
        <FlatList
          data={devices}
          renderItem={renderItem}
          keyExtractor={(item) => item.printerIPAddress}
          extraData={ipAddress}
        />
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
