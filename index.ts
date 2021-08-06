import { NativeModules } from 'react-native';

const LocalBarcodeRecognizer = NativeModules?.LocalBarcodeRecognizer;

const decode = (base64EncodedImage: string, options: any = {}): Promise<string> => {
    return LocalBarcodeRecognizer?.decode(base64EncodedImage, options);
}

export default { decode };
