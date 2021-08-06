###rn-local-qrdecode
## *** Still under development.

##Install:
```bash
#install:
yarn add @gaoyunch/rn-local-qrdecode

#and link: (RN < 0.60)
react-native link @gaoyunch/rn-local-qrdecode

```

or you may install manually.

##Usage:
Here is the demo (may check the examples folder of source code as well)

```typescript 

import RNLocalQrdecode from '@gaoyunch/rn-local-qrdecode';

const imageBase64 = "data:image/jpeg;base64,/9j/4AA.......";

type Props = {};

export default () => {

  const recoginze = async ()=>{
    // Here is the demoe
    let result = await RNLocalQrdecode.decode(imageBase64.replace("data:image/jpeg;base64,",""),{codeTypes:['ean13','qr']});
    alert(result);
  } 

  return (
    <View style={styles.container}>
      <Text style={styles.welcome}>React Native Local Barcode Recoginzer Demo</Text>
      <Text>Follow images to test:</Text>
        <Image source={{uri:imageBase64}} style={{width:width,height:width}}></Image>
        <Button onPress={recoginze} title={"Recognize"} />
    </View>
  )
}

```

## API
```javascript

 let result = await RNLocalQrdecode.decode(base64EncodeStringWithSchema,options);


```

## Options
Only codeTypes supports currently
Options:

| name | desc |
|:----:|:----:|
| codeTypes | the codeFormat array, no default values,at last one of follow values needed: aztec ean13 ean8 qr pdf417 upc_e datamatrix code39 code93 interleaved2of5 codabar code128 maxicode rss14 rssexpanded upc_a upc_ean   |
