import React, { useState, useEffect } from 'react';
import {
  AppRegistry,
  StyleSheet,
  View,
  ScrollView ,
} from 'react-native';
import {
  Provider as PaperProvider,
  DefaultTheme,
  Card,
  List,
  Button as PaperButton,
  ProgressBar, 
  Colors,
  Text,
  Snackbar,
  ActivityIndicator,
  IconButton
} from 'react-native-paper';
import {io} from "socket.io-client";

const HelloWorld = (initialProps) => {
  const ENDPOINT = "http://192.168.43.102:4001";
  const ENDPOINT1 = "http://192.168.43.102:4002";
  const [socket, setSocket] = useState(null);
  const [response, setResponse] = useState([]);
  const [actionRequested, setActionRequested] = React.useState(false);
  const [action, setAction] = React.useState({});
  const [socket1, setSocket1] = useState(null);
  const [activeStep, setActiveStep] = React.useState(0);
  const [alerte, setAlerte] = React.useState(false);
  const [infoAlerte, setInfoAlerte] = React.useState(false);
  const [percentage, setPercentage] = React.useState(0);
  const [visible, setVisible] = React.useState(false);
  const [running, setRunning] = React.useState(false);

  const onToggleSnackBar = () => {socket1.emit("Stack", action);setVisible(!visible); };

  const onDismissSnackBar = () => setVisible(false);


  useEffect(() => {
    setSocket1(io(ENDPOINT1));
    return () => io(ENDPOINT1).close();
  }, []);

  useEffect(() => {
    if(socket1){
      socket1.on("connect", (a) => {
        console.log("socket connectée");
      });
      socket1.on("AlertSeq", (a) => {
        setAction(a);
        console.log("action user : ", a);
      });
      socket1.on("Process", (a) => {
        setRunning(a);
      });
      // CLEAN UP THE EFFECT
      return () => socket1.disconnect();
    }
  }, [socket1]);

  useEffect(() => {
    setSocket(io(ENDPOINT));
    return () => io(ENDPOINT).close();
  }, []);

  useEffect(() => {
    if(socket){
      socket.on("connect", (a) => {
        console.log("socket connectée");
      });
      socket.on("FromBPAll", (a) => {
        setResponse(a);
      });
      socket1.on("ActionReqHandling", (a) => {
        setActionRequested(a);
      });
      socket.on("ActiveStep", (a) => {
        setActiveStep(a);
      });
      socket.on("Percentage", (a) => {
        setPercentage(a);
      });
      socket.on("InfoSeq", (a) => {
        setAlerte(true);
        setInfoAlerte(a);
      });
      // CLEAN UP THE EFFECT
      return () => socket.disconnect();
    }
  }, [socket]);

  return (
    <View style={{flex:1}}>
      <View style={{display:'flex', flex:1,backgroundColor:"#405366"}}>
        <View style={{ flex : running ? 9.5 : 1 }}>
          {response.length != 0 ? 
            <Card style={{flex:7, backgroundColor:"#EEEEEE", marginTop:5, marginLeft:5, marginRight:5, borderWidth:1}}>
              <Text style={{fontSize:15, color:'grey', textAlign:'center'}}>Séquence</Text>
              <ScrollView stickyHeaderIndices={[activeStep]} persistentScrollbar={true} style={styles.scrollView}>
                {response.map((value,i, arr) => {
                  return(
                    <View key={i} style={{flex:1}}>
                      {i == activeStep ? 
                        <View
                          style={{flex:1,flexDirection:'column',backgroundColor: "#FDFDFD", margin:2.5, marginLeft:"2%", marginRight:"2%", borderRadius:2, height: 140, backgroundColor: value.target == "USER" ? '#FFE477' : "lightblue"  }}
                        >
                          <View style={{flex:1, flexDirection:'row',}}>
                            <View style={{flex:3, alignItems:"center", justifyContent:'center', flexDirection:'row'}}>
                              <View style={{flex:1, alignItems:"center", justifyContent:'center'}}>
                                <Text style={{alignSelf:'flex-start', marginLeft:10}}>{value.target == "USER" ? "Action Opérateur" : "Séquence Robot"}</Text>
                              </View>
                            </View>
                            {value.target != "USER" ? <View style={{flex:1, alignItems:"center", justifyContent:'center'}}>
                              <ActivityIndicator style={{flex:1, alignItems:'center', alignSelf:'center'}} animating={true} color={Colors.blue800} />
                            </View> : null }
                          </View>
                          {value.target == "USER" ? 
                            <View style={{flex:1, flexDirection:'row'}}>
                              <View style={{flex:2, margin:5, alignItems:"center", justifyContent:'center'}}>
                                <Text style={{alignSelf:'center', textAlign:'center'}}>{value.stepStages[0].type == "LOAD.EFFECTOR" ? "Veuillez Monter l'Outil" : "Veuillez Démonter l'Outil" }</Text>
                              </View>
                              <View style={{flex:1, flexDirection:'row', alignItems:"center", justifyContent:'center'}}>
                              <PaperButton mode='outlined' onPress={onToggleSnackBar} style={{borderColor:'blue'}}>Valider</PaperButton>
                              </View>
                            </View>
                          :
                            <View style={{flex:1, flexDirection:'row', alignItems:"center", justifyContent:'center', }}>
                              <View style={{flex:3, flexDirection:'column', margin:15}}>
                                <View style={{flex:1, alignSelf:'center'}}>
                                  <Text style={{}}>?? min ?? restantes</Text>
                                </View>
                                <View style={{flex:1, }}>
                                  <ProgressBar progress={percentage} color={Colors.green800}/>
                                </View>
                              </View>
                              <View style={{flex:1, alignItems:"center"}}>
                                <Text>{(percentage*100).toFixed(0)}%</Text>
                              </View>
                            </View>
                          }

                        </View>
                      : 
                        <View
                          style={{flex:1,flexDirection:'row',backgroundColor: value.status == "SUCCESS" ? 'lightgreen' : "#FDFDFD", margin:2.5, marginLeft:"2%", marginRight:"2%", borderRadius:2, height: 70 }}
                        >
                          <View style={{flex:1, alignItems:"center", justifyContent:'center',}}>
                            <View style={{flex:2, alignItems: "center", justifyContent: value.target == "USER" ? "flex-end" : "center" , alignSelf:'flex-start'}}>
                              <Text style={{alignSelf: 'flex-start', marginLeft:10}}>{value.target == "USER" ? value.stepStages[0].type == "LOAD.EFFECTOR" ? "Monter Outil" : "Démonter Outil"  : "Séquence Robot"}</Text>
                            </View>
                              {value.target == "USER" ? <View style={{flex:1, alignItems:"center", justifyContent:'flex-start', alignSelf:'flex-start'}}>
                                <Text style={{alignSelf:'flex-start', marginLeft:20, color:'grey', textAlign:'center', fontSize:10}}>{value.target == "USER" ? "Action Opérateur" : "Séquence Robot"}</Text>
                              </View> : null}
                          </View>
                          <View style={{flex:1, alignItems:"center", justifyContent:'center'}}>
                          {value.status == "SUCCESS" ? <List.Icon style={{ flex:1, alignSelf:'flex-end'}} icon="check" color='green' /> : null}
                          </View>
                        </View>
                      }
                    </View>
                  );
                })}
              </ScrollView>
            </Card>
          :
            <Card style={{flex:7, backgroundColor:"#EEEEEE", marginTop:5, marginLeft:5, marginRight:5}}>
              <View style={{flex:1, justifyContent:'center', alignItems:'center', }}>
                <Text style={{fontSize:15, color:'grey'}}>Pas de séquence en cours</Text>
              </View>
            </Card>
          }
              {alerte ? 
                <Card style={{flex:3, backgroundColor:"white", marginTop:5, marginLeft:5, marginRight:5, borderWidth:1}}>
                  <View style={{flex:1, margin:5, backgroundColor:'red', borderWidth:2, borderRadius:5 }}>
                    <View style={{flex:1, alignItems:'center', flexDirection:'row'}}>
                      <IconButton
                        icon="alert"
                        color={Colors.black800}
                        size={20}
                        style={{flex:1, alignItems:'flex-end'}}
                      />
                      <Text icon="account" style={{fontSize:20, flex:1.4, }}>Alerte !</Text>
                    </View>
                    <View style={{flex:3, alignItems:'center', justifyContent:'center', borderWidth:1, marginLeft:15, marginRight:15}}>
                      {infoAlerte == "end" ? <Text style={{}}>Fin de la séquence</Text> : <Text style={{}}>Début de la séquence</Text>}
                    </View>
                    <View style={{flex:2, alignItems:'flex-end', justifyContent:'flex-end'}}>
                      <PaperButton color='white' style={{margin:10, }} onPress={() => {setAlerte(false);}} mode='contained'>Comfirmer !</PaperButton>
                    </View>
                  </View>
                </Card>
              :
                null
              }
        </View>
        {running &&
          <View style={{flex:0.5, marginTop:5, marginLeft:20, marginRight:20, marginBottom:5}}>
            <View style={{flex:1, flexDirection:'row'}}>
              <View style={{flex:2}}>
                <Text style={{color:'lightgreen', fontSize:25, textAlign:'center'}}>Séquence en Cours</Text>
              </View>
              <View style={{flex:1}}>
                <View style={{flex:1}}>
                  <Text style={{flex:1, color:'white', fontSize:15, textAlign:'center', justifyContent:'flex-end'}}>Fin estimée : </Text>
                </View>
                <View style={{flex:1}}>
                  <Text style={{flex:1, color:'white', fontSize:15, textAlign:'center', justifyContent:'flex-start'}}>??h??</Text>
                </View>
              </View>
            </View>
          </View>
        }
      </View>
      <Snackbar
        visible={visible}
        onDismiss={onDismissSnackBar}
        action={{
          label: 'Ok!',
          onPress: () => {
            // Do something
          },
        }}>
        Action Utilisateur Validée
      </Snackbar>
    </View>
  );
};
var styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    backgroundColor:"#33E0FF"
  },
  hello: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10
  },
  scrollView: {
    marginTop:5,
    flex:1
  },
  text: {
    fontSize: 42,
  },
});

AppRegistry.registerComponent(
  'myreactnativeapp',
  () => HelloWorld
);