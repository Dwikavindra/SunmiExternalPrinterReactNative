import { NativeModules, Platform } from 'react-native';
import type { printerDevice } from './printerDevice';

const LINKING_ERROR =
  `The package 'sunmi-external-printer' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const SunmiExternalPrinterReactNative =
  NativeModules.SunmiExternalPrinterReactNative
    ? NativeModules.SunmiExternalPrinterReactNative
    : new Proxy(
        {},
        {
          get() {
            throw new Error(LINKING_ERROR);
          },
        }
      );

export function multiply(a: number, b: number): Promise<number> {
  return SunmiExternalPrinterReactNative.multiply(a, b);
}

export const EscPosImageWithTCPConnectionRasterBitImageWrapper = async (
  base64Image: string,
  ipAddress: string,
  port: string
) => {
  try {
    return await SunmiExternalPrinterReactNative.printImageWithTCPRasterBitImageWrapper(
      base64Image,
      ipAddress,
      port
    );
  } catch (error) {
    return error;
  }
};
export const startNetworkDiscovery = async () => {
  try {
    return await SunmiExternalPrinterReactNative.startDiscovery();
  } catch (error) {
    return error;
  }
};

export const stopNetworkDiscovery = async () => {
  try {
    return await SunmiExternalPrinterReactNative.stopDiscovery();
  } catch (error) {
    return error;
  }
};

export const EscPosImageWithTCPConnectionBitImageWrapper = async (
  base64Image: string,
  ipAddress: string,
  port: string
) => {
  try {
    return await SunmiExternalPrinterReactNative.printImageWithTCPBitImageWrapper(
      base64Image,
      ipAddress,
      port
    );
  } catch (error) {
    return error;
  }
};

export const EscPosImageWithTCPConnectionGraphicsImageWrapper = async (
  base64Image: string,
  ipAddress: string,
  port: string
) => {
  try {
    return await SunmiExternalPrinterReactNative.printImageWithTCPGraphicsImageWrapper(
      base64Image,
      ipAddress,
      port
    );
  } catch (error) {
    return error;
  }
};

export const convertHTMLtoBase64 = async (html: string, width: number) => {
  try {
    return await SunmiExternalPrinterReactNative.convertHTMLtoBase64(
      html,
      width
    );
  } catch (error) {
    return error;
  }
};

export const openDrawer = async (ipAddress: string, port: string) => {
  try {
    return await SunmiExternalPrinterReactNative.openDrawer(ipAddress, port);
  } catch (error) {
    return error;
  }
};

export const scanBLDevice = async () => {
  try {
    return await SunmiExternalPrinterReactNative.scanBLDevice();
  } catch (error) {
    return error;
  }
};

export const printImageByBluetooth = async (
  device: printerDevice,
  base64Image: string
) => {
  try {
    return await SunmiExternalPrinterReactNative.printImageByBluetooth(
      device.name,
      base64Image
    );
  } catch (error) {
    return error;
  }
};
