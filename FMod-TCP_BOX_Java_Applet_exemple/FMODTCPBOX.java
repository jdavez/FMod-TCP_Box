//*************************************************************************************************
// PROJECT:		FMod-TCP BOX family																  *
//																								  *
// AUTHOR:		FiveCo 	Copyrights	2002-2010													  *
//																								  *
// REVISION: 	REV 1.0	 AG 	02.06.04	First version based on FMod-TCP applet v1.7.		  *
//				REV 1.1  AG		15.08.06	Deleted I2C 1MHz option.							  *
//				REV 1.2  AG		15.07.10	Change I2C method to RW with Ack.					  *
//											Add I2C error message management.					  *
//											Add ItemListener with Item Action.					  *
//				REV 2.0  AG		10.06.11	Add FMod-TCP BOX 2 support.							  *
//																								  *
// DESCRIPTION:	Applet designed to control FMod-TCP	BOX	family devices.							  *
//																								  *
// HOW DOES IT WORK :																			  *
// This applet is working with 2 different parts. The main process use a thread to communicate 	  *
// with the FMod-TCP BOX server board. A specific object of class ModuleVariable for each module  *
// register is used between the main process and the thread. When the main process want to read	  *
// or write a register of the module, it sets the corresponding object's boolean writeEnable or   *
// readEnable to tell the thread to write or read the module's register. For a write action, the  *
// object's table containing new register value have to be written first.						  *
//																								  *
//*************************************************************************************************

import java.awt.*;
import java.applet.*;
import java.awt.event.* ;
import java.io.*;
import java.net.*;


public class FMODTCPBOX extends Applet implements ActionListener, ItemListener, Runnable {
	
	private final boolean isDev = true;
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	// Visual interface objects
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	// Main panel *********************************************************************************
		
	// Main layout is a standart Border layout in which North, Center and South direction are used
	BorderLayout 		mainLayout 	 				= new BorderLayout() ;
	Panel  				mainPanel					= new Panel(mainLayout) ;
	
	// Main layout North direction is used for version display
	FlowLayout 			topLayout 	 				= new FlowLayout(FlowLayout.RIGHT) ;
	Panel 				topPanel		 			= new Panel(topLayout) ;
 	Label 				appletVersionLabel 			= new Label("Applet ver 2.0 10/06/11") ;
	
	// Main panel Center place is used for a card layout display for different displays
	CardLayout 			cardsLayout 	 			= new CardLayout() ;
	Panel 				cardsPanel 					= new Panel(cardsLayout) ;
	
	// Main layout South direction is used for card choose buttons
	FlowLayout 			menuLayout 	 		 		= new FlowLayout(FlowLayout.CENTER) ;
	Panel 				menuPanel		 			= new Panel(menuLayout) ;
	Button				menuPanelGoMainButton		= new Button("Main config ...") ;
	Button				menuPanelTestADIOButton		= new Button("Test AD and I/Os ...") ;
	Button				menuPanelTestUARTButton		= new Button("Test RS232 ...") ;
	Button				menuPanelTestI2CButton		= new Button("Test I2C ...") ;
	

	// Change module parameters panel card ********************************************************
  	GridBagLayout 		moduleParaPanelLayout		= new GridBagLayout() ;
  	GridBagConstraints 	moduleParaPanelLayoutConst 	= new GridBagConstraints() ;
	Panel				moduleParaPanel				= new Panel(moduleParaPanelLayout) ;
	Label				moduleParaTitle				= new Label("Main config", Label.CENTER) ;
	Label				moduleParaTypeLabel			= new Label("Device type :") ;
	Label				moduleParaTypeDispLabel		= new Label("----------------") ;
	Label				moduleParaVersionLabel		= new Label("FW version :") ;
	Label				moduleParaVersionDispLabel	= new Label("----------------") ;
  	Label				moduleParaMACLabel			= new Label("MAC address : ", Label.LEFT) ;
  	Label				moduleParaMACValueLabel		= new Label("xx-xx-xx-xx-xx-xx") ;
  	Label				moduleParaIPLabel			= new Label("IP Address :", Label.LEFT) ;
  	TextField			moduleParaIP1Text			= new TextField("192", 3) ;
  	TextField			moduleParaIP2Text			= new TextField("168", 3) ;
  	TextField			moduleParaIP3Text			= new TextField("xxx", 3) ;
  	TextField			moduleParaIP4Text			= new TextField("xxx", 3) ;
  	Label				moduleParaSNMLabel			= new Label("Subnet mask :", Label.LEFT) ;
  	TextField			moduleParaSNM1Text			= new TextField("255", 3) ;
  	TextField			moduleParaSNM2Text			= new TextField("xxx", 3) ;
  	TextField			moduleParaSNM3Text			= new TextField("xxx", 3) ;
  	TextField			moduleParaSNM4Text			= new TextField("xxx", 3) ;
  	Label				moduleParaWDLabel			= new Label("TCP timeout [s] :", Label.LEFT) ;
  	TextField			moduleParaWDText			= new TextField("xx", 3) ;
  	Label				moduleParaNameLabel			= new Label("Device name : ", Label.LEFT) ;
  	TextField			moduleParaNameText			= new TextField("Name of the module", 20) ;
	Button				moduleParaChgCommButton		= new Button("Change") ;
	Label				moduleParaBlackLine1		= new Label(" ") ;
	
  	Label				moduleParaUARTLabel			= new Label("UART Speed : ", Label.LEFT) ;
	Choice 				moduleParaBpsChoice			= new Choice() ;
  	Label				moduleParaI2CLabel			= new Label("I2C Speed : ", Label.LEFT) ;
	Choice				moduleParaI2CSpeedChoice	= new Choice() ;
  	Button				moduleParaChgBusesButton	= new Button("Change") ;
  	
	Button				moduleParaReadButton		= new Button("Read actual settings") ;
	Button				moduleParaSaveUserButton	= new Button("Save user settings") ;
	Button				moduleParaRestoreUserButton = new Button("Restore user settings") ;
	Button				moduleParaRestoreFactButton = new Button("Restore factory settings") ;
	Button				moduleParaSaveFactButton	= new Button("Save factory config") ;

	// Test AD and I/Os panel card ************************************************************
  	GridBagLayout 		testADIOPanelLayout			= new GridBagLayout() ;
  	GridBagConstraints 	testADIOPanelLayoutConst 	= new GridBagConstraints() ;
	Panel				testADIOPanel				= new Panel(testADIOPanelLayout) ;
	Label				testADIOTitle				= new Label("Test AD and I/Os", Label.CENTER) ;
	Label				testADIOInputsLabel			= new Label("Inputs :") ;
	Label				testADIOInputsStateLabel	= new Label("State") ;
	Label				testADIOInputsVoltageLabel	= new Label("Voltage") ;
	Label				testADIOIn0Label			= new Label("1", Label.RIGHT) ;
	Label				testADIOIn1Label			= new Label("2", Label.RIGHT) ;
	Label				testADIOIn2Label			= new Label("3", Label.RIGHT) ;
	Label				testADIOIn3Label			= new Label("4", Label.RIGHT) ;
	Label				testADIOIn4Label			= new Label("5", Label.RIGHT) ;
	Label				testADIOIn5Label			= new Label("6", Label.RIGHT) ;
	Label				testADIOIn6Label			= new Label("7", Label.RIGHT) ;
	Label				testADIOIn7Label			= new Label("8", Label.RIGHT) ;
	Label				testADIOIn8Label			= new Label("9", Label.RIGHT) ;
	Label				testADIOIn9Label			= new Label("10", Label.RIGHT) ;
	Label				testADIOInALabel			= new Label("11", Label.RIGHT) ;
	Label				testADIOInBLabel			= new Label("12", Label.RIGHT) ;
	Label				testADIOInCLabel			= new Label("13", Label.RIGHT) ;
	Label				testADIOInDLabel			= new Label("14", Label.RIGHT) ;
	Label				testADIOInELabel			= new Label("15", Label.RIGHT) ;
	Label				testADIOInFLabel			= new Label("16", Label.RIGHT) ;
	
	Label				testADIOInState0Label 		= new Label("--") ;
	Label				testADIOInState1Label 		= new Label("--") ;
	Label				testADIOInState2Label 		= new Label("--") ;
	Label				testADIOInState3Label 		= new Label("--") ;
	Label				testADIOInState4Label 		= new Label("--") ;
	Label				testADIOInState5Label 		= new Label("--") ;
	Label				testADIOInState6Label 		= new Label("--") ;
	Label				testADIOInState7Label 		= new Label("--") ;
	Label				testADIOInState8Label 		= new Label("--") ;
	Label				testADIOInState9Label 		= new Label("--") ;
	Label				testADIOInStateALabel 		= new Label("--") ;
	Label				testADIOInStateBLabel 		= new Label("--") ;
	Label				testADIOInStateCLabel 		= new Label("--") ;
	Label				testADIOInStateDLabel 		= new Label("--") ;
	Label				testADIOInStateELabel 		= new Label("--") ;
	Label				testADIOInStateFLabel 		= new Label("--") ;
	
	Label				testADIOAD0ValueLabel 		= new Label("------") ;
	Label				testADIOAD1ValueLabel 		= new Label("------") ;
	Label				testADIOAD2ValueLabel 		= new Label("------") ;
	Label				testADIOAD3ValueLabel 		= new Label("------") ;
	Label				testADIOAD4ValueLabel 		= new Label("------") ;
	Label				testADIOAD5ValueLabel 		= new Label("------") ;
	Label				testADIOAD6ValueLabel 		= new Label("------") ;
	Label				testADIOAD7ValueLabel 		= new Label("------") ;
	Label				testADIOAD8ValueLabel 		= new Label("------") ;
	Label				testADIOAD9ValueLabel 		= new Label("------") ;
	Label				testADIOADAValueLabel 		= new Label("------") ;
	Label				testADIOADBValueLabel 		= new Label("------") ;
	Label				testADIOADCValueLabel 		= new Label("------") ;
	Label				testADIOADDValueLabel 		= new Label("------") ;
	Label				testADIOADEValueLabel 		= new Label("------") ;
	Label				testADIOADFValueLabel 		= new Label("------") ;
	
	Label				testADIOADThresholdLabel	= new Label("Analog threshold [V] :") ;
	TextField			testADIOADThresholdText		= new TextField("------") ;
	
	Label				testADIOBlackColumn			= new Label(" ") ;
	
	Label				testADIOOutputsLabel		= new Label("Outputs :") ;
	Choice				testADIOOutF				= new Choice() ;
	Choice				testADIOOutE				= new Choice() ;
	Choice				testADIOOutD				= new Choice() ;
	Choice				testADIOOutC				= new Choice() ;
	Choice				testADIOOutB				= new Choice() ;
	Choice				testADIOOutA				= new Choice() ;
	Choice				testADIOOut9				= new Choice() ;
	Choice				testADIOOut8				= new Choice() ;
	Choice				testADIOOut7				= new Choice() ;
	Choice				testADIOOut6				= new Choice() ;
	Choice				testADIOOut5				= new Choice() ;
	Choice				testADIOOut4				= new Choice() ;
	Choice				testADIOOut3				= new Choice() ;
	Choice				testADIOOut2				= new Choice() ;
	Choice				testADIOOut1				= new Choice() ;
	Choice				testADIOOut0				= new Choice() ;
	
	Button				testADIOWriteButton			= new Button("Write") ;
	
	Label				testADIOBlackColumn2		= new Label(" ") ;
	Label				testADIOSupplyLabel			= new Label("Supply voltage :            ") ;
	Label				testADIOSupplyDispLabel		= new Label("--------") ;
	Label				testADIOBlackLine1			= new Label(" ") ;
	Label				testADIOWarningLabel		= new Label("Warnings :") ;
	Label				testADIOWarningDispLabel	= new Label("----------------------------") ;

	// Test UART panel card ************************************************************
  	GridBagLayout 		testUARTPanelLayout			= new GridBagLayout() ;
  	GridBagConstraints 	testUARTPanelLayoutConst 	= new GridBagConstraints() ;
	Panel				testUARTPanel				= new Panel(testUARTPanelLayout) ;
	Label				testUARTTitle				= new Label("Test UART", Label.CENTER) ;
	Label				testUARTTxLabel				= new Label("Transmit to UART :") ;
	TextField			testUARTTxText				= new TextField("Not connected") ;
	Button				testUARTTxSendButton		= new Button("Send") ;
	Label				testUARTRxLabel				= new Label("Receive from UART :") ;
	TextArea			testUARTRxTA				= new TextArea(17,50) ;
  	Checkbox			testUARTLFCB				= new Checkbox("Add LF", false) ;
  	Checkbox			testUARTCRCB				= new Checkbox("Add CR", false) ;
  	Checkbox			testUARTNullCB				= new Checkbox("Add NULL", false) ;
	Button				testUARTConnectButton 		= new Button("Connect to RS232 TCP port") ;

	// Test I2C panel card ************************************************************
  	GridBagLayout 		testI2CPanelLayout			= new GridBagLayout() ;
  	GridBagConstraints 	testI2CPanelLayoutConst 	= new GridBagConstraints() ;
	Panel				testI2CPanel				= new Panel(testI2CPanelLayout) ;
	Label				testI2CTitle				= new Label("Test I2C", Label.CENTER) ;
	Label				testI2CNbByteReadLabel		= new Label("Number of bytes to read :") ;
	TextField			testI2CNbByteReadText		= new TextField("1") ;
	Label				testI2CHex2WriteLabel		= new Label("Hex data to write (ex: A2 3F) :") ;
	TextField			testI2CHex2WriteText		= new TextField("") ;
	Label				testI2CAddLabel				= new Label("I2C address (7bits hexa) :") ;
	TextField			testI2CAddText				= new TextField("1") ;
	Button				testI2CSendButton			= new Button("Send") ;
	Label				testI2CDataRxLabel			= new Label("Data received :") ;
	TextArea			testI2CDataRxTA				= new TextArea(16,50) ;
	
	Label				testI2CBlackLine1			= new Label("") ;
	
	Button				testI2CScanButton			= new Button("Scan bus") ;
	Label				testI2CScanAnswerLabel		= new Label("I2C devices found :") ;
	List				testI2CScanAnswerList		= new List(15) ;

	/////////////////////////////////////////////////////////////////////////////////////////////
	// IP DC Mot variables
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	// Bank 0 : General Info 
	final int		TCPSERVERTYPE						= 0x00 ;
	final int		TCPSERVERVERSION					= 0x01 ;
	final int		TCPSERVERRESETPROC					= 0x02 ; // function
	final int		TCPSERVERSAVEUSERPARAMETERS			= 0x03 ; // function
	final int		TCPSERVERRESTOREUSERPARAMETERS		= 0x04 ; // function
	final int		TCPSERVERRESTOREFACTORYPARAMETERS	= 0x05 ; // function
	final int		TCPSERVERSAVEFACTORYPARAMETERS		= 0x06 ; // function
	final int		TCPSERVERVOLTAGE					= 0x07 ;
	final int		TCPSERVERWARNING					= 0x08 ;
	
	// Bank 1 : Communication
	final int		TCPSERVERCOMOPTIONS					= 0x10 ;
	final int		TCPSERVERMAC						= 0x11 ;
	final int		TCPSERVERIP							= 0x12 ;
	final int		TCPSERVERSUBNETMASK					= 0x13 ;
	final int		TCPSERVERTCPWATCHDOG				= 0x14 ;
	final int		TCPSERVERNAME						= 0x15 ;
	final int		TCPSERVERUARTCONFIG					= 0x16 ;
	final int		TCPSERVERI2CSPDCONFIG				= 0x18 ;
	final int		TCPSERVERNUMBEROFUSERS				= 0x1A ;
	
	// Bank 2 : External ports
	final int		TCPSERVERINANALOGTHRESHOLD			= 0x20 ;
	final int		TCPSERVEROUTPUTS					= 0x21 ;
	final int		TCPSERVERINPUTS						= 0x23 ;
	
	// Bank 3 : AD
	final int		TCPSERVERAD0VALUE					= 0x30 ;
	final int		TCPSERVERAD1VALUE					= 0x31 ;
	final int		TCPSERVERAD2VALUE					= 0x32 ;
	final int		TCPSERVERAD3VALUE					= 0x33 ;
	final int		TCPSERVERAD4VALUE					= 0x34 ;
	final int		TCPSERVERAD5VALUE					= 0x35 ;
	final int		TCPSERVERAD6VALUE					= 0x36 ;
	final int		TCPSERVERAD7VALUE					= 0x37 ;
	final int		TCPSERVERAD8VALUE					= 0x38 ;
	final int		TCPSERVERAD9VALUE					= 0x39 ;
	final int		TCPSERVERADAVALUE					= 0x3A ;
	final int		TCPSERVERADBVALUE					= 0x3B ;
	final int		TCPSERVERADCVALUE					= 0x3C ;
	final int		TCPSERVERADDVALUE					= 0x3D ;
	final int		TCPSERVERADEVALUE					= 0x3E ;
	final int		TCPSERVERADFVALUE					= 0x3F ;
	
	// Bank 0 : General Info 												(address,length)
	ModuleVariable	TCPServerType						= new ModuleVariable(TCPSERVERTYPE,4) ;
	ModuleVariable	TCPServerVersion					= new ModuleVariable(TCPSERVERVERSION,4) ;
	ModuleVariable	TCPServerResetProc					= new ModuleVariable(TCPSERVERRESETPROC,0) ; 				// function
	ModuleVariable	TCPServerSaveUserParameters			= new ModuleVariable(TCPSERVERSAVEUSERPARAMETERS,0) ; 		// function
	ModuleVariable	TCPServerRestoreUserParameters		= new ModuleVariable(TCPSERVERRESTOREUSERPARAMETERS,0) ; 	// function
	ModuleVariable	TCPServerRestoreFactoryParameters	= new ModuleVariable(TCPSERVERRESTOREFACTORYPARAMETERS,0) ; // function
	ModuleVariable	TCPServerSaveFactoryParameters		= new ModuleVariable(TCPSERVERSAVEFACTORYPARAMETERS,0) ; 	// function
	ModuleVariable	TCPServerVoltage					= new ModuleVariable(TCPSERVERVOLTAGE,4) ;
	ModuleVariable	TCPServerWarning					= new ModuleVariable(TCPSERVERWARNING,4) ;
	
	// Bank 1 : Communication
	ModuleVariable	TCPServerComOptions					= new ModuleVariable(TCPSERVERCOMOPTIONS,4) ;
	ModuleVariable	TCPServerMAC						= new ModuleVariable(TCPSERVERMAC,6) ;
	ModuleVariable	TCPServerIP							= new ModuleVariable(TCPSERVERIP,4) ;
	ModuleVariable	TCPServerSubnetMask					= new ModuleVariable(TCPSERVERSUBNETMASK,4) ;
	ModuleVariable	TCPServerTCPWatchdog				= new ModuleVariable(TCPSERVERTCPWATCHDOG,1) ;
	ModuleVariable	TCPServerName						= new ModuleVariable(TCPSERVERNAME,16) ;
	ModuleVariable	TCPServerUARTConfig					= new ModuleVariable(TCPSERVERUARTCONFIG,1) ;
	ModuleVariable	TCPServerI2CSpdConfig				= new ModuleVariable(TCPSERVERI2CSPDCONFIG,1) ;
	ModuleVariable	TCPServerNumberOfUsers				= new ModuleVariable(TCPSERVERNUMBEROFUSERS,1);
	
	// Bank 2 : Communication
	ModuleVariable	TCPServerInAnalogThreshold			= new ModuleVariable(TCPSERVERINANALOGTHRESHOLD,4) ;
	ModuleVariable	TCPServerOutputs					= new ModuleVariable(TCPSERVEROUTPUTS,2) ;
	ModuleVariable	TCPServerInputs						= new ModuleVariable(TCPSERVERINPUTS,2) ;
	
	// Bank 3 : AD
	ModuleVariable	TCPServerAD0Value					= new ModuleVariable(TCPSERVERAD0VALUE,4) ;
	ModuleVariable	TCPServerAD1Value					= new ModuleVariable(TCPSERVERAD1VALUE,4) ;
	ModuleVariable	TCPServerAD2Value					= new ModuleVariable(TCPSERVERAD2VALUE,4) ;
	ModuleVariable	TCPServerAD3Value					= new ModuleVariable(TCPSERVERAD3VALUE,4) ;
	ModuleVariable	TCPServerAD4Value					= new ModuleVariable(TCPSERVERAD4VALUE,4) ;
	ModuleVariable	TCPServerAD5Value					= new ModuleVariable(TCPSERVERAD5VALUE,4) ;
	ModuleVariable	TCPServerAD6Value					= new ModuleVariable(TCPSERVERAD6VALUE,4) ;
	ModuleVariable	TCPServerAD7Value					= new ModuleVariable(TCPSERVERAD7VALUE,4) ;
	ModuleVariable	TCPServerAD8Value					= new ModuleVariable(TCPSERVERAD8VALUE,4) ;
	ModuleVariable	TCPServerAD9Value					= new ModuleVariable(TCPSERVERAD9VALUE,4) ;
	ModuleVariable	TCPServerADAValue					= new ModuleVariable(TCPSERVERADAVALUE,4) ;
	ModuleVariable	TCPServerADBValue					= new ModuleVariable(TCPSERVERADBVALUE,4) ;
	ModuleVariable	TCPServerADCValue					= new ModuleVariable(TCPSERVERADCVALUE,4) ;
	ModuleVariable	TCPServerADDValue					= new ModuleVariable(TCPSERVERADDVALUE,4) ;
	ModuleVariable	TCPServerADEValue					= new ModuleVariable(TCPSERVERADEVALUE,4) ;
	ModuleVariable	TCPServerADFValue					= new ModuleVariable(TCPSERVERADFVALUE,4) ;

	/////////////////////////////////////////////////////////////////////////////////////////////
	// Other objects and variables
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	Thread 			IPConnectionThread	;
	boolean			IPConnectionKeepOpen			= true ;
	
	final int 		TCPUARTPORT 					= 8000;
	final int 		TCPMOTORPORT 					= 8010;
	String 			moduleIPAddress 				= "169.254.5.5" ; // default module address
	Socket 			uartControlSocket 				= null;	
	Socket 			mainPortSocket 					= null;	
	int				uniqueID 						= 0 ;
	int				loopState 						= 0 ;
	boolean			waitingAnswer					= false ;
	boolean			I2CCommand						= false ;
	byte			I2CCommandType					= 0 ;
	byte[] 			I2CCommandBuffer				= new byte[100];
	int				I2CCommandBufferLength			= 0;
	boolean			I2CScanCommand					= false ;
	
	final float		AnalogRatio						= (float)14.124 ;//14.152542372881355932203389830508 ;
	

	/////////////////////////////////////////////////////////////////////////////////////////////
	// Applet standart functions
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	// Init function
	public void init() {
		setSize(550,550) ;	
		setFont(new Font("Arial", Font.PLAIN, 12)) ;	
		
		// Module parameters panel card build ******************************************************
		moduleParaPanelLayoutConst.insets = new Insets(2,3,2,3) ;
		
		moduleParaTitle.setFont(new Font("Arial", Font.BOLD, 15)) ;
		moduleParaTitle.setBackground(Color.black) ;
		moduleParaTitle.setForeground(Color.white) ;
		buildConstraints(moduleParaPanelLayoutConst, 0, 0, 6, 1, 0,10, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER) ;
		moduleParaPanelLayout.setConstraints(moduleParaTitle, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaTitle) ;
		
		buildConstraints(moduleParaPanelLayoutConst, 0, 1, 1, 1, 0, 0, GridBagConstraints.NONE, GridBagConstraints.WEST) ;
		moduleParaPanelLayout.setConstraints(moduleParaTypeLabel, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaTypeLabel) ;
		buildConstraints(moduleParaPanelLayoutConst, 1, 1, 4, 1, 0, 0, GridBagConstraints.NONE, GridBagConstraints.WEST) ;
		moduleParaPanelLayout.setConstraints(moduleParaTypeDispLabel, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaTypeDispLabel) ;
		buildConstraints(moduleParaPanelLayoutConst, 0, 2, 1, 1, 0, 0, GridBagConstraints.NONE, GridBagConstraints.WEST) ;
		moduleParaPanelLayout.setConstraints(moduleParaVersionLabel, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaVersionLabel) ;
		buildConstraints(moduleParaPanelLayoutConst, 1, 2, 4, 1, 0, 0, GridBagConstraints.NONE, GridBagConstraints.WEST) ;
		moduleParaPanelLayout.setConstraints(moduleParaVersionDispLabel, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaVersionDispLabel) ;
		
		buildConstraints(moduleParaPanelLayoutConst, 0, 3, 1, 1, 0,10, GridBagConstraints.NONE, GridBagConstraints.WEST) ;
		moduleParaPanelLayout.setConstraints(moduleParaMACLabel, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaMACLabel) ;
		buildConstraints(moduleParaPanelLayoutConst, 1, 3, 2, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST) ;
		moduleParaPanelLayout.setConstraints(moduleParaMACValueLabel, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaMACValueLabel) ;
		
		buildConstraints(moduleParaPanelLayoutConst, 0, 4, 1, 1,20,10, GridBagConstraints.NONE, GridBagConstraints.WEST) ;
		moduleParaPanelLayout.setConstraints(moduleParaIPLabel, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaIPLabel) ;
		buildConstraints(moduleParaPanelLayoutConst, 1, 4, 1, 1,12, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST) ;
		moduleParaPanelLayout.setConstraints(moduleParaIP1Text, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaIP1Text) ;
		buildConstraints(moduleParaPanelLayoutConst, 2, 4, 1, 1,12, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST) ;
		moduleParaPanelLayout.setConstraints(moduleParaIP2Text, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaIP2Text) ;
		buildConstraints(moduleParaPanelLayoutConst, 3, 4, 1, 1,12, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST) ;
		moduleParaPanelLayout.setConstraints(moduleParaIP3Text, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaIP3Text) ;
		buildConstraints(moduleParaPanelLayoutConst, 4, 4, 1, 1,12, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST) ;
		moduleParaPanelLayout.setConstraints(moduleParaIP4Text, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaIP4Text) ;
		
		buildConstraints(moduleParaPanelLayoutConst, 0, 5, 1, 1,20,10, GridBagConstraints.NONE, GridBagConstraints.WEST) ;
		moduleParaPanelLayout.setConstraints(moduleParaSNMLabel, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaSNMLabel) ;
		buildConstraints(moduleParaPanelLayoutConst, 1, 5, 1, 1,12, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST) ;
		moduleParaPanelLayout.setConstraints(moduleParaSNM1Text, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaSNM1Text) ;
		buildConstraints(moduleParaPanelLayoutConst, 2, 5, 1, 1,12, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST) ;
		moduleParaPanelLayout.setConstraints(moduleParaSNM2Text, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaSNM2Text) ;
		buildConstraints(moduleParaPanelLayoutConst, 3, 5, 1, 1,12, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST) ;
		moduleParaPanelLayout.setConstraints(moduleParaSNM3Text, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaSNM3Text) ;
		buildConstraints(moduleParaPanelLayoutConst, 4, 5, 1, 1,12, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST) ;
		moduleParaPanelLayout.setConstraints(moduleParaSNM4Text, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaSNM4Text) ;
		
		buildConstraints(moduleParaPanelLayoutConst, 0, 6, 1, 1, 0,10, GridBagConstraints.NONE, GridBagConstraints.WEST) ;
		moduleParaPanelLayout.setConstraints(moduleParaWDLabel, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaWDLabel) ;
		buildConstraints(moduleParaPanelLayoutConst, 1, 6, 4, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST) ;
		moduleParaPanelLayout.setConstraints(moduleParaWDText, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaWDText) ;
		
		buildConstraints(moduleParaPanelLayoutConst, 0, 7, 1, 1, 0,10, GridBagConstraints.NONE, GridBagConstraints.WEST) ;
		moduleParaPanelLayout.setConstraints(moduleParaNameLabel, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaNameLabel) ;
		buildConstraints(moduleParaPanelLayoutConst, 1, 7, 4, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST) ;
		moduleParaPanelLayout.setConstraints(moduleParaNameText, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaNameText) ;
		
		buildConstraints(moduleParaPanelLayoutConst, 5, 4, 1, 4,36, 0, GridBagConstraints.BOTH, GridBagConstraints.CENTER) ;
		moduleParaPanelLayout.setConstraints(moduleParaChgCommButton, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaChgCommButton) ;
		
		buildConstraints(moduleParaPanelLayoutConst, 0, 8, 1, 1, 0,10, GridBagConstraints.NONE, GridBagConstraints.WEST) ;
		moduleParaPanelLayout.setConstraints(moduleParaUARTLabel, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaUARTLabel) ;
		buildConstraints(moduleParaPanelLayoutConst, 1, 8, 4, 1, 0,10, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST) ;
		moduleParaPanelLayout.setConstraints(moduleParaBpsChoice, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaBpsChoice) ;
		
		buildConstraints(moduleParaPanelLayoutConst, 0, 9, 1, 1, 0,10, GridBagConstraints.NONE, GridBagConstraints.WEST) ;
		moduleParaPanelLayout.setConstraints(moduleParaI2CLabel, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaI2CLabel) ;
		buildConstraints(moduleParaPanelLayoutConst, 1, 9, 4, 1, 0,10, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST) ;
		moduleParaPanelLayout.setConstraints(moduleParaI2CSpeedChoice, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaI2CSpeedChoice) ;	
		
		buildConstraints(moduleParaPanelLayoutConst, 5, 8, 1, 2, 0, 0, GridBagConstraints.BOTH, GridBagConstraints.CENTER) ;
		moduleParaPanelLayout.setConstraints(moduleParaChgBusesButton, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaChgBusesButton) ;
		
		moduleParaBlackLine1.setFont(new Font("Arial", Font.BOLD, 1)) ;
		moduleParaBlackLine1.setBackground(Color.black) ;
		buildConstraints(moduleParaPanelLayoutConst, 0,10, 7, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER) ;
		moduleParaPanelLayout.setConstraints(moduleParaBlackLine1, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaBlackLine1) ;
				
		buildConstraints(moduleParaPanelLayoutConst, 0,11, 3, 1, 0,10, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		moduleParaPanelLayout.setConstraints(moduleParaReadButton, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaReadButton) ;
		buildConstraints(moduleParaPanelLayoutConst, 3,11, 3, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		moduleParaPanelLayout.setConstraints(moduleParaSaveUserButton, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaSaveUserButton) ;
		buildConstraints(moduleParaPanelLayoutConst, 0,12, 3, 1, 0,10, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		moduleParaPanelLayout.setConstraints(moduleParaRestoreUserButton, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaRestoreUserButton) ;
		buildConstraints(moduleParaPanelLayoutConst, 3,12, 3, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		moduleParaPanelLayout.setConstraints(moduleParaRestoreFactButton, moduleParaPanelLayoutConst) ;
		moduleParaPanel.add(moduleParaRestoreFactButton) ;
				
		
		// Test AD and I/Os panel card build ***************************************************	
		testADIOPanelLayoutConst.insets = new Insets(0,5,0,5) ;
		
		testADIOTitle.setFont(new Font("Arial", Font.BOLD, 15)) ;
		testADIOTitle.setBackground(Color.black) ;
		testADIOTitle.setForeground(Color.white) ;
		buildConstraints(testADIOPanelLayoutConst, 0, 0, 7, 1, 0,10, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER) ;
		testADIOPanelLayout.setConstraints(testADIOTitle, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOTitle) ;
		
		buildConstraints(testADIOPanelLayoutConst, 0, 1, 1, 1, 0,10, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOInputsLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOInputsLabel) ;
		buildConstraints(testADIOPanelLayoutConst, 1, 1, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOInputsStateLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOInputsStateLabel) ;
		buildConstraints(testADIOPanelLayoutConst, 2, 1, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOInputsVoltageLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOInputsVoltageLabel) ;
		
		buildConstraints(testADIOPanelLayoutConst, 0, 2, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOIn0Label, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOIn0Label) ;
		buildConstraints(testADIOPanelLayoutConst, 0, 3, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOIn1Label, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOIn1Label) ;
		buildConstraints(testADIOPanelLayoutConst, 0, 4, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOIn2Label, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOIn2Label) ;
		buildConstraints(testADIOPanelLayoutConst, 0, 5, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOIn3Label, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOIn3Label) ;
		buildConstraints(testADIOPanelLayoutConst, 0, 6, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOIn4Label, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOIn4Label) ;
		buildConstraints(testADIOPanelLayoutConst, 0, 7, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOIn5Label, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOIn5Label) ;
		buildConstraints(testADIOPanelLayoutConst, 0, 8, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOIn6Label, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOIn6Label) ;
		buildConstraints(testADIOPanelLayoutConst, 0, 9, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOIn7Label, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOIn7Label) ;
		buildConstraints(testADIOPanelLayoutConst, 0,10, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOIn8Label, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOIn8Label) ;
		buildConstraints(testADIOPanelLayoutConst, 0,11, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOIn9Label, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOIn9Label) ;
		buildConstraints(testADIOPanelLayoutConst, 0,12, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOInALabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOInALabel) ;
		buildConstraints(testADIOPanelLayoutConst, 0,13, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOInBLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOInBLabel) ;
		buildConstraints(testADIOPanelLayoutConst, 0,14, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOInCLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOInCLabel) ;
		buildConstraints(testADIOPanelLayoutConst, 0,15, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOInDLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOInDLabel) ;
		buildConstraints(testADIOPanelLayoutConst, 0,16, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOInELabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOInELabel) ;
		buildConstraints(testADIOPanelLayoutConst, 0,17, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOInFLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOInFLabel) ;
				
		buildConstraints(testADIOPanelLayoutConst, 1, 2, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOInState0Label, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOInState0Label) ;
		buildConstraints(testADIOPanelLayoutConst, 1, 3, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOInState1Label, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOInState1Label) ;
		buildConstraints(testADIOPanelLayoutConst, 1, 4, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOInState2Label, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOInState2Label) ;
		buildConstraints(testADIOPanelLayoutConst, 1, 5, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOInState3Label, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOInState3Label) ;
		buildConstraints(testADIOPanelLayoutConst, 1, 6, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOInState4Label, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOInState4Label) ;
		buildConstraints(testADIOPanelLayoutConst, 1, 7, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOInState5Label, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOInState5Label) ;
		buildConstraints(testADIOPanelLayoutConst, 1, 8, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOInState6Label, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOInState6Label) ;
		buildConstraints(testADIOPanelLayoutConst, 1, 9, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOInState7Label, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOInState7Label) ;
		buildConstraints(testADIOPanelLayoutConst, 1,10, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOInState8Label, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOInState8Label) ;
		buildConstraints(testADIOPanelLayoutConst, 1,11, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOInState9Label, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOInState9Label) ;
		buildConstraints(testADIOPanelLayoutConst, 1,12, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOInStateALabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOInStateALabel) ;
		buildConstraints(testADIOPanelLayoutConst, 1,13, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOInStateBLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOInStateBLabel) ;
		buildConstraints(testADIOPanelLayoutConst, 1,14, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOInStateCLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOInStateCLabel) ;
		buildConstraints(testADIOPanelLayoutConst, 1,15, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOInStateDLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOInStateDLabel) ;
		buildConstraints(testADIOPanelLayoutConst, 1,16, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOInStateELabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOInStateELabel) ;
		buildConstraints(testADIOPanelLayoutConst, 1,17, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOInStateFLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOInStateFLabel) ;
		
		buildConstraints(testADIOPanelLayoutConst, 2, 2, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOAD0ValueLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOAD0ValueLabel) ;
		buildConstraints(testADIOPanelLayoutConst, 2, 3, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOAD1ValueLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOAD1ValueLabel) ;
		buildConstraints(testADIOPanelLayoutConst, 2, 4, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOAD2ValueLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOAD2ValueLabel) ;
		buildConstraints(testADIOPanelLayoutConst, 2, 5, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOAD3ValueLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOAD3ValueLabel) ;
		buildConstraints(testADIOPanelLayoutConst, 2, 6, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOAD4ValueLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOAD4ValueLabel) ;
		buildConstraints(testADIOPanelLayoutConst, 2, 7, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOAD5ValueLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOAD5ValueLabel) ;
		buildConstraints(testADIOPanelLayoutConst, 2, 8, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOAD6ValueLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOAD6ValueLabel) ;
		buildConstraints(testADIOPanelLayoutConst, 2, 9, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOAD7ValueLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOAD7ValueLabel) ;
		buildConstraints(testADIOPanelLayoutConst, 2,10, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOAD8ValueLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOAD8ValueLabel) ;
		buildConstraints(testADIOPanelLayoutConst, 2,11, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOAD9ValueLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOAD9ValueLabel) ;
		buildConstraints(testADIOPanelLayoutConst, 2,12, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOADAValueLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOADAValueLabel) ;
		buildConstraints(testADIOPanelLayoutConst, 2,13, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOADBValueLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOADBValueLabel) ;
		buildConstraints(testADIOPanelLayoutConst, 2,14, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOADCValueLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOADCValueLabel) ;
		buildConstraints(testADIOPanelLayoutConst, 2,15, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOADDValueLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOADDValueLabel) ;
		buildConstraints(testADIOPanelLayoutConst, 2,16, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOADEValueLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOADEValueLabel) ;
		buildConstraints(testADIOPanelLayoutConst, 2,17, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOADFValueLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOADFValueLabel) ;
		
		buildConstraints(testADIOPanelLayoutConst, 0,18, 2, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOADThresholdLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOADThresholdLabel) ;
		buildConstraints(testADIOPanelLayoutConst, 2,18, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOADThresholdText, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOADThresholdText) ;
		
		testADIOBlackColumn.setFont(new Font("Arial", Font.BOLD, 1)) ;
		testADIOBlackColumn.setBackground(Color.black) ;
		buildConstraints(testADIOPanelLayoutConst, 3, 1, 1,17, 0, 0, GridBagConstraints.VERTICAL, GridBagConstraints.CENTER) ;
		testADIOPanelLayout.setConstraints(testADIOBlackColumn, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOBlackColumn) ;
			
		buildConstraints(testADIOPanelLayoutConst, 4, 1, 1, 1, 0,10, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOOutputsLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOOutputsLabel) ;
						
		buildConstraints(testADIOPanelLayoutConst, 4, 2, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOOut0, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOOut0) ;
		buildConstraints(testADIOPanelLayoutConst, 4, 3, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOOut1, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOOut1) ;
		buildConstraints(testADIOPanelLayoutConst, 4, 4, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOOut2, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOOut2) ;
		buildConstraints(testADIOPanelLayoutConst, 4, 5, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOOut3, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOOut3) ;
		buildConstraints(testADIOPanelLayoutConst, 4, 6, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOOut4, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOOut4) ;
		buildConstraints(testADIOPanelLayoutConst, 4, 7, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOOut5, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOOut5) ;
		buildConstraints(testADIOPanelLayoutConst, 4, 8, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOOut6, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOOut6) ;
		buildConstraints(testADIOPanelLayoutConst, 4, 9, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOOut7, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOOut7) ;
		buildConstraints(testADIOPanelLayoutConst, 4,10, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOOut8, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOOut8) ;
		buildConstraints(testADIOPanelLayoutConst, 4,11, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOOut9, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOOut9) ;
		buildConstraints(testADIOPanelLayoutConst, 4,12, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOOutA, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOOutA) ;
		buildConstraints(testADIOPanelLayoutConst, 4,13, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOOutB, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOOutB) ;
		buildConstraints(testADIOPanelLayoutConst, 4,14, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOOutC, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOOutC) ;
		buildConstraints(testADIOPanelLayoutConst, 4,15, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOOutD, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOOutD) ;
		buildConstraints(testADIOPanelLayoutConst, 4,16, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOOutE, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOOutE) ;
		buildConstraints(testADIOPanelLayoutConst, 4,17, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOOutF, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOOutF) ;
		
		buildConstraints(testADIOPanelLayoutConst, 4,18, 1, 1, 0, 0, GridBagConstraints.BOTH, GridBagConstraints.CENTER) ;
		testADIOPanelLayout.setConstraints(testADIOWriteButton, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOWriteButton) ;
		
		testADIOBlackColumn2.setFont(new Font("Arial", Font.BOLD, 1)) ;
		testADIOBlackColumn2.setBackground(Color.black) ;
		buildConstraints(testADIOPanelLayoutConst, 5, 1, 1,18, 0, 0, GridBagConstraints.VERTICAL, GridBagConstraints.CENTER) ;
		testADIOPanelLayout.setConstraints(testADIOBlackColumn2, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOBlackColumn2) ;
		
		buildConstraints(testADIOPanelLayoutConst, 6, 1, 1, 1, 0,10, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOSupplyLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOSupplyLabel) ;
		buildConstraints(testADIOPanelLayoutConst, 6, 2, 1, 1, 0,10, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOSupplyDispLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOSupplyDispLabel) ;
		
		testADIOBlackLine1.setFont(new Font("Arial", Font.BOLD, 1)) ;
		testADIOBlackLine1.setBackground(Color.black) ;
		buildConstraints(testADIOPanelLayoutConst, 6, 3, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER) ;
		testADIOPanelLayout.setConstraints(testADIOBlackLine1, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOBlackLine1) ;
		
		buildConstraints(testADIOPanelLayoutConst, 6, 4, 1, 1, 0,10, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOWarningLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOWarningLabel) ;
		buildConstraints(testADIOPanelLayoutConst, 6, 5, 1, 6, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testADIOPanelLayout.setConstraints(testADIOWarningDispLabel, testADIOPanelLayoutConst) ;
		testADIOPanel.add(testADIOWarningDispLabel) ;
		
		// Test UART panel card build ***************************************************	
		testUARTPanelLayoutConst.insets = new Insets(2,3,2,3) ;
		
		testUARTTitle.setFont(new Font("Arial", Font.BOLD, 15)) ;
		testUARTTitle.setBackground(Color.black) ;
		testUARTTitle.setForeground(Color.white) ;
		buildConstraints(testUARTPanelLayoutConst, 0, 0, 5, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER) ;
		testUARTPanelLayout.setConstraints(testUARTTitle, testUARTPanelLayoutConst) ;
		testUARTPanel.add(testUARTTitle) ;
		
		buildConstraints(testUARTPanelLayoutConst, 0, 1, 5, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testUARTPanelLayout.setConstraints(testUARTConnectButton, testUARTPanelLayoutConst) ;
		testUARTPanel.add(testUARTConnectButton) ;
	
		buildConstraints(testUARTPanelLayoutConst, 0, 2, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.SOUTH) ;
		testUARTPanelLayout.setConstraints(testUARTTxLabel, testUARTPanelLayoutConst) ;
		testUARTPanel.add(testUARTTxLabel) ;
		buildConstraints(testUARTPanelLayoutConst, 1, 2, 4, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.SOUTH) ;
		testUARTPanelLayout.setConstraints(testUARTTxText, testUARTPanelLayoutConst) ;
		testUARTPanel.add(testUARTTxText) ;
		
		buildConstraints(testUARTPanelLayoutConst, 1, 3, 1, 1, 0, 0, GridBagConstraints.NONE, GridBagConstraints.WEST) ;
		testUARTPanelLayout.setConstraints(testUARTLFCB, testUARTPanelLayoutConst) ;
		testUARTPanel.add(testUARTLFCB) ;
		buildConstraints(testUARTPanelLayoutConst, 2, 3, 1, 1, 0, 0, GridBagConstraints.NONE, GridBagConstraints.WEST) ;
		testUARTPanelLayout.setConstraints(testUARTCRCB, testUARTPanelLayoutConst) ;
		testUARTPanel.add(testUARTCRCB) ;
		buildConstraints(testUARTPanelLayoutConst, 3, 3, 1, 1, 0, 0, GridBagConstraints.NONE, GridBagConstraints.WEST) ;
		testUARTPanelLayout.setConstraints(testUARTNullCB, testUARTPanelLayoutConst) ;
		testUARTPanel.add(testUARTNullCB) ;
		buildConstraints(testUARTPanelLayoutConst, 4, 3, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testUARTPanelLayout.setConstraints(testUARTTxSendButton, testUARTPanelLayoutConst) ;
		testUARTPanel.add(testUARTTxSendButton) ;
		
		buildConstraints(testUARTPanelLayoutConst, 0, 4, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH) ;
		testUARTPanelLayout.setConstraints(testUARTRxLabel, testUARTPanelLayoutConst) ;
		testUARTPanel.add(testUARTRxLabel) ;
		buildConstraints(testUARTPanelLayoutConst, 1, 4, 4, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST) ;
		testUARTPanelLayout.setConstraints(testUARTRxTA, testUARTPanelLayoutConst) ;
		testUARTPanel.add(testUARTRxTA) ;
		
		
		// Test I2C panel card build ***************************************************	
		testI2CPanelLayoutConst.insets = new Insets(2,3,2,3) ;
		
		testI2CTitle.setFont(new Font("Arial", Font.BOLD, 15)) ;
		testI2CTitle.setBackground(Color.black) ;
		testI2CTitle.setForeground(Color.white) ;
		buildConstraints(testI2CPanelLayoutConst, 0, 0, 5, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER) ;
		testI2CPanelLayout.setConstraints(testI2CTitle, testI2CPanelLayoutConst) ;
		testI2CPanel.add(testI2CTitle) ;
	
		buildConstraints(testI2CPanelLayoutConst, 1, 1, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.SOUTH) ;
		testI2CPanelLayout.setConstraints(testI2CNbByteReadLabel, testI2CPanelLayoutConst) ;
		testI2CPanel.add(testI2CNbByteReadLabel) ;
		buildConstraints(testI2CPanelLayoutConst, 2, 1, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.SOUTH) ;
		testI2CPanelLayout.setConstraints(testI2CNbByteReadText, testI2CPanelLayoutConst) ;
		testI2CPanel.add(testI2CNbByteReadText) ;
	
		buildConstraints(testI2CPanelLayoutConst, 1, 2, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.SOUTH) ;
		testI2CPanelLayout.setConstraints(testI2CHex2WriteLabel, testI2CPanelLayoutConst) ;
		testI2CPanel.add(testI2CHex2WriteLabel) ;
		buildConstraints(testI2CPanelLayoutConst, 2, 2, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.SOUTH) ;
		testI2CPanelLayout.setConstraints(testI2CHex2WriteText, testI2CPanelLayoutConst) ;
		testI2CPanel.add(testI2CHex2WriteText) ;
	
		buildConstraints(testI2CPanelLayoutConst, 1, 3, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.SOUTH) ;
		testI2CPanelLayout.setConstraints(testI2CAddLabel, testI2CPanelLayoutConst) ;
		testI2CPanel.add(testI2CAddLabel) ;
		buildConstraints(testI2CPanelLayoutConst, 2, 3, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.SOUTH) ;
		testI2CPanelLayout.setConstraints(testI2CAddText, testI2CPanelLayoutConst) ;
		testI2CPanel.add(testI2CAddText) ;
		
		buildConstraints(testI2CPanelLayoutConst, 2, 4, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.SOUTH) ;
		testI2CPanelLayout.setConstraints(testI2CSendButton, testI2CPanelLayoutConst) ;
		testI2CPanel.add(testI2CSendButton) ;
		
		buildConstraints(testI2CPanelLayoutConst, 0, 4, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.SOUTH) ;
		testI2CPanelLayout.setConstraints(testI2CDataRxLabel, testI2CPanelLayoutConst) ;
		testI2CPanel.add(testI2CDataRxLabel) ;
		buildConstraints(testI2CPanelLayoutConst, 0, 5, 3, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.SOUTH) ;
		testI2CPanelLayout.setConstraints(testI2CDataRxTA, testI2CPanelLayoutConst) ;
		testI2CPanel.add(testI2CDataRxTA) ;
		
		testI2CBlackLine1.setBackground(Color.black) ;
		buildConstraints(testI2CPanelLayoutConst, 3, 1, 1, 5, 0, 0, GridBagConstraints.VERTICAL, GridBagConstraints.SOUTH) ;
		testI2CPanelLayout.setConstraints(testI2CBlackLine1, testI2CPanelLayoutConst) ;
		testI2CPanel.add(testI2CBlackLine1) ;
		
		buildConstraints(testI2CPanelLayoutConst, 4, 1, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER) ;
		testI2CPanelLayout.setConstraints(testI2CScanButton, testI2CPanelLayoutConst) ;
		testI2CPanel.add(testI2CScanButton) ;
		buildConstraints(testI2CPanelLayoutConst, 4, 2, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER) ;
		testI2CPanelLayout.setConstraints(testI2CScanAnswerLabel, testI2CPanelLayoutConst) ;
		testI2CPanel.add(testI2CScanAnswerLabel) ;
		buildConstraints(testI2CPanelLayoutConst, 4, 3, 1, 3, 0, 0, GridBagConstraints.BOTH, GridBagConstraints.CENTER) ;
		testI2CPanelLayout.setConstraints(testI2CScanAnswerList, testI2CPanelLayoutConst) ;
		testI2CPanel.add(testI2CScanAnswerList) ;
		
		// Main panel build ***********************************************************************
		this.setBackground(Color.white) ;
		
		moduleParaBpsChoice.add("4800 bds") ;
		moduleParaBpsChoice.add("9600 bds") ;
		moduleParaBpsChoice.add("19200 bds") ;
		moduleParaBpsChoice.add("38400 bds") ;
		moduleParaBpsChoice.add("57600 bds") ;
		moduleParaBpsChoice.add("115200 bds") ;
		moduleParaBpsChoice.add("4800 bds with flow ctrl") ;
		moduleParaBpsChoice.add("9600 bds with flow ctrl") ;
		moduleParaBpsChoice.add("19200 bds with flow ctrl") ;
		moduleParaBpsChoice.add("38400 bds with flow ctrl") ;
		moduleParaBpsChoice.add("57600 bds with flow ctrl") ;
		moduleParaBpsChoice.add("115200 bds with flow ctrl") ;
		moduleParaI2CSpeedChoice.add("100 kHz") ;
		moduleParaI2CSpeedChoice.add("400 kHz (read manual !)") ;
		
		testADIOOutF.add("High");
		testADIOOutE.add("High");
		testADIOOutD.add("High");
		testADIOOutC.add("High");
		testADIOOutB.add("High");
		testADIOOutA.add("High");
		testADIOOut9.add("High");
		testADIOOut8.add("High");
		testADIOOut7.add("High");
		testADIOOut6.add("High");
		testADIOOut5.add("High");
		testADIOOut4.add("High");
		testADIOOut3.add("High");
		testADIOOut2.add("High");
		testADIOOut1.add("High");
		testADIOOut0.add("High");
		testADIOOutF.add("Low");
		testADIOOutE.add("Low");
		testADIOOutD.add("Low");
		testADIOOutC.add("Low");
		testADIOOutB.add("Low");
		testADIOOutA.add("Low");
		testADIOOut9.add("Low");
		testADIOOut8.add("Low");
		testADIOOut7.add("Low");
		testADIOOut6.add("Low");
		testADIOOut5.add("Low");
		testADIOOut4.add("Low");
		testADIOOut3.add("Low");
		testADIOOut2.add("Low");
		testADIOOut1.add("Low");
		testADIOOut0.add("Low");

		topPanel.add(appletVersionLabel) ;
		
		cardsPanel.add("moduleParaPanel", moduleParaPanel) ;
		cardsPanel.add("testADIOPanel", testADIOPanel) ;
		cardsPanel.add("testUARTPanel", testUARTPanel) ;
		cardsPanel.add("testI2CPanel", testI2CPanel) ;
		
		menuPanel.add(menuPanelGoMainButton) ;
		menuPanel.add(menuPanelTestADIOButton) ;
		menuPanel.add(menuPanelTestUARTButton) ;
		menuPanel.add(menuPanelTestI2CButton) ;
		menuPanel.setBackground(Color.white) ;
		
		mainPanel.removeAll() ;
		mainPanel.add(topPanel,BorderLayout.NORTH) ;
		mainPanel.add(menuPanel,BorderLayout.CENTER) ;
		mainPanel.add(cardsPanel,BorderLayout.SOUTH) ;
		add(mainPanel) ;
		
		// Add Action listener ********************************************************************
		
		moduleParaReadButton.addActionListener(this) ;
		moduleParaSaveUserButton.addActionListener(this) ;
		moduleParaRestoreUserButton.addActionListener(this) ;
		moduleParaRestoreFactButton.addActionListener(this) ;
		moduleParaSaveFactButton.addActionListener(this) ;
		
		moduleParaChgCommButton.addActionListener(this) ;
  		moduleParaChgBusesButton.addActionListener(this) ;
		
		testADIOWriteButton.addActionListener(this) ;
		
		testUARTTxSendButton.addActionListener(this) ;
		testUARTConnectButton.addActionListener(this) ;
		testUARTTxText.setEnabled(false) ;
	
		testI2CSendButton.addActionListener(this) ;
		testI2CScanButton.addActionListener(this) ;
		testI2CScanAnswerList.addItemListener(this) ;
		
		menuPanelGoMainButton.setFont(new Font("Arial", Font.BOLD, 15)) ;
		menuPanelGoMainButton.setBackground(Color.black) ;
		menuPanelGoMainButton.setForeground(Color.white) ;
		menuPanelGoMainButton.addActionListener(this) ;
		menuPanelTestADIOButton.setFont(new Font("Arial", Font.BOLD, 15)) ;
		menuPanelTestADIOButton.setBackground(Color.black) ;
		menuPanelTestADIOButton.setForeground(Color.white) ;
		menuPanelTestADIOButton.addActionListener(this) ;
		menuPanelTestUARTButton.setFont(new Font("Arial", Font.BOLD, 15)) ;
		menuPanelTestUARTButton.setBackground(Color.black) ;
		menuPanelTestUARTButton.setForeground(Color.white) ;
		menuPanelTestUARTButton.addActionListener(this) ;
		menuPanelTestI2CButton.setFont(new Font("Arial", Font.BOLD, 15)) ;
		menuPanelTestI2CButton.setBackground(Color.black) ;
		menuPanelTestI2CButton.setForeground(Color.white) ;
		menuPanelTestI2CButton.addActionListener(this) ;
	}
	
	// Start method *******************************************************************************
	public void start() {
		
		// Get module IP address
		if (getCodeBase().getHost() != "") {
			moduleIPAddress = getCodeBase().getHost() ;
		}
		
		// Create main port socket
		try {
			mainPortSocket = new Socket(moduleIPAddress, TCPMOTORPORT);
		} catch (IOException e) {
			System.err.println("Exception : can't create mainPortSocket ! " + e.getMessage());
		}
		
		// Start IP connection thread if it does not exist
		if (IPConnectionThread == null) {
			IPConnectionThread	= new Thread(this) ;
			IPConnectionThread.start() ;
		}
	}
	
	// Stop method ********************************************************************************
	public void stop() {
	
		IPConnectionKeepOpen = false ;	
		
		// Disconnect from motion control port
		try { 
			mainPortSocket.close() ;
		} catch (IOException e) {	
			System.err.println("Exception : can't close mainPortSocket ! " + e.getMessage());
		}
		
		// Disconnect from uart port
		if (uartControlSocket!=null) {
			try { 
				uartControlSocket.close() ;
			} catch (IOException e) {	
				System.err.println("Exception : can't close uartControlSocket ! " + e.getMessage());
			}
		}
	}

	// Paint method *******************************************************************************
	public void paint(Graphics g) {
	}

	/////////////////////////////////////////////////////////////////////////////////////////////
	// Actions management
	/////////////////////////////////////////////////////////////////////////////////////////////
	public void actionPerformed(ActionEvent evt) {
		
		float consignVal = 0;
		Object source = evt.getSource() ;
		String temp = new String();
		char charTemp ;
		int	intTemp, intTemp2 = 0;
		float floatTemp = 0 ;
		
		// Menu buttons ***************************************************************************
		if (source == menuPanelGoMainButton) {
			TCPServerVersion.readEnable = true ;
			TCPServerComOptions.readEnable = true ;
			TCPServerMAC.readEnable = true ;
			TCPServerName.readEnable = true ;
			TCPServerIP.readEnable = true ;
			TCPServerSubnetMask.readEnable = true ;
			TCPServerTCPWatchdog.readEnable = true ;
			TCPServerUARTConfig.readEnable = true ;
			cardsLayout.show(cardsPanel,"moduleParaPanel") ;
		} else if (source == menuPanelTestADIOButton) {
			TCPServerOutputs.readEnable = true ;
			TCPServerInAnalogThreshold.readEnable = true ;
			cardsLayout.show(cardsPanel,"testADIOPanel") ;
		} else if (source == menuPanelTestUARTButton) {
			cardsLayout.show(cardsPanel,"testUARTPanel") ;
		} else if (source == menuPanelTestI2CButton) {
			I2CScanCommand = true ;
			cardsLayout.show(cardsPanel,"testI2CPanel") ;
						
		// Main parameters panel ******************************************************************
		} else if (source == moduleParaReadButton) {
			TCPServerName.readEnable = true ;
			TCPServerIP.readEnable = true ;
			TCPServerTCPWatchdog.readEnable = true ;
			TCPServerUARTConfig.readEnable = true ;
			TCPServerI2CSpdConfig.readEnable = true ;
		} else if (source == moduleParaSaveUserButton) {
			TCPServerSaveUserParameters.writeEnable = true ;	
		} else if (source == moduleParaRestoreUserButton) {
			TCPServerRestoreUserParameters.writeEnable = true ;
		} else if (source == moduleParaRestoreFactButton) {
			TCPServerRestoreFactoryParameters.writeEnable = true ;
		} else if (source == moduleParaSaveFactButton) {
			TCPServerSaveFactoryParameters.writeEnable = true ;

		} else if (source == moduleParaChgCommButton) {
			if ((Integer.valueOf(moduleParaIP1Text.getText()).intValue()>0) &
				(Integer.valueOf(moduleParaIP2Text.getText()).intValue()>0) &
				(Integer.valueOf(moduleParaIP3Text.getText()).intValue()>0) &
				(Integer.valueOf(moduleParaIP4Text.getText()).intValue()>0) &
				(Integer.valueOf(moduleParaIP1Text.getText()).intValue()<255) &
				(Integer.valueOf(moduleParaIP2Text.getText()).intValue()<255) &
				(Integer.valueOf(moduleParaIP3Text.getText()).intValue()<255) &
				(Integer.valueOf(moduleParaIP4Text.getText()).intValue()<255)) {
					TCPServerIP.setIntValue((Integer.valueOf(moduleParaIP1Text.getText()).intValue()<<24)+(Integer.valueOf(moduleParaIP2Text.getText()).intValue()<<16)+(Integer.valueOf(moduleParaIP3Text.getText()).intValue()<<8)+(Integer.valueOf(moduleParaIP4Text.getText()).intValue())) ;
					TCPServerIP.writeEnable = true ;
			} else {
				moduleParaIP1Text.setText("169") ;
				moduleParaIP2Text.setText("254") ;
				moduleParaIP3Text.setText("5") ;
				moduleParaIP4Text.setText("5") ;
			}
			if ((Integer.valueOf(moduleParaSNM1Text.getText()).intValue()>=0) &
				(Integer.valueOf(moduleParaSNM2Text.getText()).intValue()>=0) &
				(Integer.valueOf(moduleParaSNM3Text.getText()).intValue()>=0) &
				(Integer.valueOf(moduleParaSNM4Text.getText()).intValue()>=0) &
				(Integer.valueOf(moduleParaSNM1Text.getText()).intValue()<=255) &
				(Integer.valueOf(moduleParaSNM2Text.getText()).intValue()<=255) &
				(Integer.valueOf(moduleParaSNM3Text.getText()).intValue()<=255) &
				(Integer.valueOf(moduleParaSNM4Text.getText()).intValue()<=255)) {
			//		if ((moduleParaIP1Text.getText().intValue()&0x80) ==  )			; Checker le subnet mask en fonction de la class
					TCPServerSubnetMask.setIntValue((Integer.valueOf(moduleParaSNM1Text.getText()).intValue()<<24)+(Integer.valueOf(moduleParaSNM2Text.getText()).intValue()<<16)+(Integer.valueOf(moduleParaSNM3Text.getText()).intValue()<<8)+(Integer.valueOf(moduleParaSNM4Text.getText()).intValue())) ;
					TCPServerSubnetMask.writeEnable = true ;
			} else {
				moduleParaSNM1Text.setText("255") ;
				moduleParaSNM2Text.setText("255") ;
				moduleParaSNM3Text.setText("0") ;
				moduleParaSNM4Text.setText("0") ;
			}
			if ((Integer.valueOf(moduleParaWDText.getText()).intValue()==0) || ((Integer.valueOf(moduleParaWDText.getText()).intValue()>=30) & (Integer.valueOf(moduleParaWDText.getText()).intValue()<256))) {
				TCPServerTCPWatchdog.setIntValue(Integer.valueOf(moduleParaWDText.getText()).intValue()) ;
				TCPServerTCPWatchdog.writeEnable = true ;
			} else {
				moduleParaWDText.setText("30") ;
			}
			if (moduleParaNameText.getText().length() > 16) {
				temp = moduleParaNameText.getText().substring(0,15) ;
				moduleParaNameText.setText(temp) ;
			} else {
				temp = moduleParaNameText.getText() ;
			}
			for (int i=0 ; i<16; i++) {
				if (i < moduleParaNameText.getText().length())
					TCPServerName.values[i+1] = (temp.substring(i,i+1).getBytes())[0] ;		
				else
					TCPServerName.values[i+1] = 0x20 ;
			}
			TCPServerName.writeEnable = true ;
			
		} else if (source==moduleParaChgBusesButton) {
			if (moduleParaBpsChoice.getSelectedIndex()<6) {
				intTemp = moduleParaBpsChoice.getSelectedIndex() ;
			} else {
				intTemp = moduleParaBpsChoice.getSelectedIndex()-6+0x80 ;
			}
			TCPServerUARTConfig.setIntValue(intTemp) ;
			TCPServerUARTConfig.writeEnable = true ;
			
			if (moduleParaI2CSpeedChoice.getSelectedIndex()==1)
				TCPServerI2CSpdConfig.setIntValue(24) ;
			else
				TCPServerI2CSpdConfig.setIntValue(99) ;
			TCPServerI2CSpdConfig.writeEnable = true ;
			
		// Test AD and I/Os panel *****************************************************************
		} else if (source == testADIOWriteButton) {
			intTemp = 0 ;
			if (testADIOOutF.getSelectedItem() == "High") {
				intTemp += 0x8000 ;
			}
			if (testADIOOutE.getSelectedItem() == "High") {
				intTemp += 0x4000 ;
			}
			if (testADIOOutD.getSelectedItem() == "High") {
				intTemp += 0x2000 ;
			}
			if (testADIOOutC.getSelectedItem() == "High") {
				intTemp += 0x1000 ;
			}
			if (testADIOOutB.getSelectedItem() == "High") {
				intTemp += 0x0800 ;
			}
			if (testADIOOutA.getSelectedItem() == "High") {
				intTemp += 0x0400 ;
			}
			if (testADIOOut9.getSelectedItem() == "High") {
				intTemp += 0x0200 ;
			}
			if (testADIOOut8.getSelectedItem() == "High") {
				intTemp += 0x0100 ;
			}
			if (testADIOOut7.getSelectedItem() == "High") {
				intTemp += 0x80 ;
			}
			if (testADIOOut6.getSelectedItem() == "High") {
				intTemp += 0x40 ;
			}
			if (testADIOOut5.getSelectedItem() == "High") {
				intTemp += 0x20 ;
			}
			if (testADIOOut4.getSelectedItem() == "High") {
				intTemp += 0x10 ;
			}
			if (testADIOOut3.getSelectedItem() == "High") {
				intTemp += 0x08 ;
			}
			if (testADIOOut2.getSelectedItem() == "High") {
				intTemp += 0x04 ;
			}
			if (testADIOOut1.getSelectedItem() == "High") {
				intTemp += 0x02 ;
			}
			if (testADIOOut0.getSelectedItem() == "High") {
				intTemp += 0x01 ;
			}
				System.err.println(intTemp2) ;
				
			TCPServerOutputs.setIntValue(intTemp) ;
			TCPServerOutputs.writeEnable = true ;
			
			intTemp = (int)(Float.valueOf(testADIOADThresholdText.getText()).floatValue()*65536) ;
			TCPServerInAnalogThreshold.setIntValue(intTemp) ;
			TCPServerInAnalogThreshold.writeEnable = true ;
			
		// Test UART panel ************************************************************************
		} else if (source == testUARTTxSendButton) {
			try {
		  	  	DataOutputStream out = new DataOutputStream(uartControlSocket.getOutputStream()) ;
		  	  	temp = testUARTTxText.getText() ;
				if (testUARTCRCB.getState())
					temp += "\r" ;
		  	  	if (testUARTLFCB.getState())
					temp += "\n" ;
				if (testUARTNullCB.getState()) {
					charTemp = 0 ;
					temp += charTemp ;
				}
				out.writeBytes(temp);
			} catch (IOException e) {
				System.err.println("Exception : can't write to uartControlSocket ! " + e.getMessage());
				testUARTTxText.setEnabled(false) ;
				testUARTTxText.setText("Not connected") ;
				testUARTConnectButton.setEnabled(true) ;
				uartControlSocket = null ;
			}
		} else if (source == testUARTConnectButton) {
			try {
				uartControlSocket = new Socket(moduleIPAddress, TCPUARTPORT);
				testUARTTxText.setEnabled(true) ;
				testUARTConnectButton.setEnabled(false) ;
			} catch (IOException e) {
				System.err.println("Exception : can't create uartControlSocket ! " + e.getMessage());
			}
			
		// Test I2C Panel *************************************************************************
		} else if(source == testI2CSendButton) {
			I2CCommandBufferLength = 2 ;
			I2CCommandBuffer[0] = (byte)Integer.valueOf(testI2CAddText.getText(),16).intValue() ; //I2C add
			I2CCommandBuffer[1] = (byte)ReadI2CData2Write(I2CCommandBuffer, I2CCommandBufferLength) ; // Number of data to write first
			I2CCommandBufferLength+=I2CCommandBuffer[1] ;
			I2CCommandType = 0x07 ;	// I2C Read/Write with ack
			I2CCommandBuffer[I2CCommandBufferLength++] = (byte)Integer.valueOf(testI2CNbByteReadText.getText()).intValue(); // Number of bytes to read  
			I2CCommand = true ;
		} else if(source == testI2CScanButton) {
			I2CScanCommand = true ;
		}
	}
	
	// Item selection management ******************************************************************
	public void itemStateChanged(ItemEvent evt) {
		Object source = evt.getSource() ;
		boolean boolTemp ;
			
		if (source == testI2CScanAnswerList) {
			testI2CAddText.setText(testI2CScanAnswerList.getItem(testI2CScanAnswerList.getSelectedIndex()));
			
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	// IP connection thread and functions
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	public void run() {
		
		boolean boolTemp;
	
		TCPServerType.readEnable = true ;
		TCPServerVersion.readEnable = true ;
		TCPServerMAC.readEnable = true ;
		
		TCPServerName.readEnable = true ;
		TCPServerIP.readEnable = true ;
		TCPServerTCPWatchdog.readEnable = true ;
		TCPServerUARTConfig.readEnable = true ;
		TCPServerI2CSpdConfig.readEnable = true ;
		TCPServerComOptions.readEnable = true ;
		TCPServerSubnetMask.readEnable = true ;
		
		
		// Main loop of the thread. Executed every 100ms
		// There is 3 states in this loop :
		// - write to be performed
		// - read to be performed
		// - waiting for an answer from server
		while (IPConnectionKeepOpen == true) {
			
			try {
				Thread.sleep(25) ;
			} catch (InterruptedException e) {}
			
			
			switch (loopState) {
				// Case 0 : a write to the module has to be performed.
				case 0 :	WriteToTCP() ;
							loopState = 1 ;
							break ;
				// Case 1 : waiting for an answer from the module.
				case 1 :	if (GetTCPData())
								loopState = 2 ;
							break;
				// Case 1 : a read from the module has to be performed.
				case 2 :	ReadToTCP() ;
							loopState = 3 ;
							//if (!isDev) {
								TCPServerAD0Value.readEnable = true ;
								TCPServerAD1Value.readEnable = true ;
								TCPServerAD2Value.readEnable = true ;
								TCPServerAD3Value.readEnable = true ;
								TCPServerAD4Value.readEnable = true ;
								TCPServerAD5Value.readEnable = true ;
								TCPServerAD6Value.readEnable = true ;
								TCPServerAD7Value.readEnable = true ;
								TCPServerAD8Value.readEnable = true ;
								TCPServerAD9Value.readEnable = true ;
								TCPServerADAValue.readEnable = true ;
								TCPServerADBValue.readEnable = true ;
								TCPServerADCValue.readEnable = true ;
								TCPServerADDValue.readEnable = true ;
								TCPServerADEValue.readEnable = true ;
								TCPServerADFValue.readEnable = true ;
								TCPServerInputs.readEnable = true ;
								TCPServerVoltage.readEnable = true ;
								TCPServerWarning.readEnable = true ;
							//}
							break ;
				// Case 3 : waiting for an answer from the module.
				case 3 :	if (GetTCPData())
								loopState = 4 ;
							break ; 
				// Case 4 : Send I2C command.
				case 4 : 	if (I2CCommand)
								SendI2CCommand() ;
							I2CCommand = false ;
							loopState = 5 ;
							break ; 
				// Case 5 : Check I2C answer.
				case 5 : 	if (GetTCPData())
								loopState = 6 ;
							break ; 
				// Case 6 : Send I2C Scan command.
				case 6 : 	if (I2CScanCommand)
								SendI2CScanCommand() ;
							I2CScanCommand = false ;
							loopState = 7 ;
							break ;
				// Case 7 : Check I2C Scan answer.
				case 7 :	if (GetTCPData())
								loopState = 0 ;
							break ;
				default : 	loopState = 0 ;
			}	
		}
		// End of main loop
	}
	
	// TCP Connection functions *******************************************************************
	// Send a write packet to module
	void WriteToTCP() {
		
		byte[] 			buffer							= new byte[2000];
		int				bufferIndex 					= 6 ;
		int 			checksum ;
	
		// For each TCPServer variables, check if a write is necessary
		bufferIndex = TCPServerType.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerVersion.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerComOptions.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerIP.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerSubnetMask.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerMAC.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerName.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerTCPWatchdog.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerUARTConfig.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerI2CSpdConfig.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerSaveUserParameters.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerRestoreUserParameters.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerRestoreFactoryParameters.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerSaveFactoryParameters.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerVoltage.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerWarning.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerNumberOfUsers.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerInAnalogThreshold.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerOutputs.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerInputs.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerAD0Value.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerAD1Value.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerAD2Value.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerAD3Value.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerAD4Value.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerAD5Value.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerAD6Value.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerAD7Value.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerAD8Value.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerAD9Value.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerADAValue.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerADBValue.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerADCValue.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerADDValue.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerADEValue.TestWriteAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerADFValue.TestWriteAndFillBuffer(buffer, bufferIndex) ;
						
		if (bufferIndex > 6) {
			buffer[0] = 0x00 ;
			buffer[1] = 0x22 ;
			buffer[2] = (byte)((uniqueID&0xff00)>>8) ;
			buffer[3] = (byte)((uniqueID++&0x00ff)) ;
			buffer[4] = (byte)(((bufferIndex-6)&0xff00)>>8) ;
			buffer[5] = (byte)(((bufferIndex-6)&0x00ff)) ;
			
			checksum = calcChecksum(buffer, bufferIndex) ;
			
			buffer[bufferIndex++] = (byte)((checksum>>8)&0xFF) ;
			buffer[bufferIndex++] = (byte)(checksum&0xFF) ;
			
			waitingAnswer = true ;
			
			try {
		  		DataOutputStream out = new DataOutputStream(mainPortSocket.getOutputStream()) ;
				out.write(buffer,0,bufferIndex);
	//			out = null ; ;		
			} catch (IOException e) {
				System.err.println("Exception : can't write to mainPortSocket ! " + e.getMessage());
			}
		}
	}
	
	// Send a read packet to module
	void ReadToTCP() {
		
		byte[] 			buffer							= new byte[2000];
		int[]			bufferIndex 					= new int[2] ;	// First int for bufferIndex and second int for answer buf length
		int 			checksum ;
	
		bufferIndex[0] = 6 ;
	
		// For each TCPServer variables, check if a write is necessary
		bufferIndex = TCPServerType.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerVersion.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerComOptions.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerIP.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerSubnetMask.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerMAC.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerName.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerTCPWatchdog.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerUARTConfig.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerI2CSpdConfig.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerSaveUserParameters.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerRestoreUserParameters.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerRestoreFactoryParameters.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerSaveFactoryParameters.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerVoltage.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerWarning.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerNumberOfUsers.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerInAnalogThreshold.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerOutputs.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerInputs.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerAD0Value.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerAD1Value.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerAD2Value.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerAD3Value.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerAD4Value.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerAD5Value.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerAD6Value.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerAD7Value.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerAD8Value.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerAD9Value.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerADAValue.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerADBValue.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerADCValue.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerADDValue.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerADEValue.TestReadAndFillBuffer(buffer, bufferIndex) ;
		bufferIndex = TCPServerADFValue.TestReadAndFillBuffer(buffer, bufferIndex) ;
				
		if (bufferIndex[0] > 6) {
			buffer[0] = 0x00 ;
			buffer[1] = 0x21 ;
			buffer[2] = (byte)((uniqueID&0xff00)>>8) ;
			buffer[3] = (byte)((uniqueID++&0x00ff)) ;
			buffer[4] = (byte)(((bufferIndex[0]-6)&0xff00)>>8) ;
			buffer[5] = (byte)(((bufferIndex[0]-6)&0x00ff)) ;
			
			checksum = calcChecksum(buffer, bufferIndex[0]) ;
			
			buffer[bufferIndex[0]++] = (byte)((checksum>>8)&0xFF) ;
			buffer[bufferIndex[0]++] = (byte)(checksum&0xFF) ;
			
			waitingAnswer = true ;
			
			try {
		  	  DataOutputStream out = new DataOutputStream(mainPortSocket.getOutputStream()) ;
				out.write(buffer,0,bufferIndex[0]);
		
			} catch (IOException e) {
				System.err.println("Exception : can't write to mainSocket ! " + e.getMessage());
			}
		}		 
	}
	
	// Send I2C command packet
	void SendI2CCommand() {
		
		byte[] 			buffer			= new byte[108];
		int				bufferIndex 	= 6;
		int 			checksum ;
			
			buffer[0] = 0x00 ;
			buffer[1] = I2CCommandType ;
			buffer[2] = (byte)((uniqueID&0xff00)>>8) ;
			buffer[3] = (byte)((uniqueID++&0x00ff)) ;
			buffer[4] = (byte)((I2CCommandBufferLength&0xff00)>>8) ;
			buffer[5] = (byte)((I2CCommandBufferLength&0x00ff)) ;
			
			for (int i=0; i<I2CCommandBufferLength ; i++){
				buffer[bufferIndex++] = I2CCommandBuffer[i] ;
			}
			
			checksum = calcChecksum(buffer, bufferIndex) ;
			
			buffer[bufferIndex++] = (byte)((checksum>>8)&0xFF) ;
			buffer[bufferIndex++] = (byte)(checksum&0xFF) ;
			
			I2CCommandBufferLength = 0;
			waitingAnswer = true ;

			try {
		  		DataOutputStream out = new DataOutputStream(mainPortSocket.getOutputStream()) ;
				out.write(buffer,0,bufferIndex);
			} catch (IOException e) {
				System.err.println("Exception : can't write to mainPortSocket ! " + e.getMessage());
			}
	}
	
	// Send I2C scan command packet
	void SendI2CScanCommand() {
		
		byte[] 			buffer			= new byte[8];
		int 			checksum ;
			
			buffer[0] = 0x00 ;
			buffer[1] = 0x05 ;
			buffer[2] = (byte)((uniqueID&0xff00)>>8) ;
			buffer[3] = (byte)((uniqueID++&0x00ff)) ;
			buffer[4] = 0x00 ;
			buffer[5] = 0x00 ;
			
			checksum = calcChecksum(buffer, 6) ;
			
			buffer[6] = (byte)((checksum>>8)&0xFF) ;
			buffer[7] = (byte)(checksum&0xFF) ;
			
			waitingAnswer = true ;

			try {
		  		DataOutputStream out = new DataOutputStream(mainPortSocket.getOutputStream()) ;
				out.write(buffer,0,8);
			} catch (IOException e) {
				System.err.println("Exception : can't write to mainPortSocket ! " + e.getMessage());
			}
	}
	
	// Get data from the module
	boolean GetTCPData() {
		
		byte[] 			buffer							= new byte[2000];
		byte[] 			uartBuffer						= new byte[500];
		int 			dataLength ;
		int 			uartDataLength ;
		int				checksumIndex ;
		int 			checksum, checksumReceived ;
		boolean			result ;
		
		
		result = true ;
		
		if (waitingAnswer) {
			waitingAnswer = false ;
			
			// Get data from TCP buffer
			try {
				DataInputStream  is  = new DataInputStream (mainPortSocket.getInputStream());
				dataLength = is.read(buffer);
				
				if (dataLength != 0) {
						
					checksumIndex 	 = ((((int)(buffer[4]))<<8)&0xFF00)+(((int)buffer[5])&0xFF)+6 ;
					checksumReceived = ((((int)(buffer[checksumIndex]))<<8)&0xFF00)+(((int)buffer[checksumIndex+1])&0xFF) ;
					checksum	  	 = calcChecksum(buffer, dataLength-2) ;//buffer.length-2) ; marche pas ???
				
					// if it's a good packet continue
					if ((buffer[0] == 0) && (checksumReceived == checksum)) {
						switch 	(buffer[1]) {
							case 0x23 :	DisplayData(buffer, dataLength) ;
										break ;
							case 0x24 :	
										break ;
							case 0x06 : DisplayI2CScanData(buffer, dataLength) ;
										break ;
							case 0x08 : DisplayI2CData(buffer, dataLength) ;
										break ;
							case 0x09 : DisplayI2CError(buffer, dataLength) ;
										break ;
							default	:	result = false ;
										break ;
						}
					}
				}
			//	is.close() ;
			//	is = null ;
			} catch (IOException e) {
				System.err.println("Exception : can't read from mainPortSocket ! " + e.getMessage());
			}
		}
		
		// Get data from uart TCP port
		if (uartControlSocket!=null) {
			try {
				DataInputStream  uartis  = new DataInputStream (uartControlSocket.getInputStream());
				if (uartis.available()!=0) {
					uartDataLength = uartis.read(uartBuffer);
					
					for (int i=0; i<uartDataLength; i++) {
						testUARTRxTA.append(""+(char)uartBuffer[i]) ;
					}
				}
			//	uartis = null ;
			} catch (IOException e) {
				System.err.println("Exception : can't read from uartControlSocket ! " + e.getMessage());
				testUARTTxText.setEnabled(false) ;
				testUARTTxText.setText("Not connected") ;
				testUARTConnectButton.setEnabled(true) ;
				uartControlSocket = null ;
			}
		}
		
		return result ;
	}
	
	
	// Display data read from TCP to visual interface
	void DisplayData(byte[] buffer, int bufferLength) {
		
		int anDebug[] = new int[255];
		int nDebugIndex = 0;
		int bufferIndex	= 6, temp ;
		String stringTemp = "" ;
		float tempFloat ;
		
		bufferLength -= 2 ;
		
		while (bufferIndex<bufferLength) {
			if (isDev) {
				anDebug[nDebugIndex++] = buffer[bufferIndex];
			}
			switch (buffer[bufferIndex++]) {
														// Version and Type should always be read in the same packet
				case TCPSERVERTYPE					:	temp = ConvertVariableBufferToInt(buffer, bufferIndex, 4) ;
														bufferIndex += 4 ;
														if (temp == 0x00080000)
															stringTemp = "FMod-TCP BOX";
														else if (temp == 0x00080002)
															stringTemp = "FMod-TCP BOX 2";
														else
															stringTemp = "Unknown";													
														moduleParaTypeDispLabel.setText(stringTemp);
														break ;
				case TCPSERVERVERSION				:	temp = ConvertVariableBufferToInt(buffer, bufferIndex, 4) ;
														bufferIndex += 4 ;
														if ((temp & 0xFF00FF00) == 0)
															moduleParaVersionDispLabel.setText((temp>>16) + "." + (temp&0xFFFF)) ;
														else {
															moduleParaVersionLabel.setText("HW/FW version:");
															moduleParaVersionDispLabel.setText(((temp>>24)&0xFF) + "." + ((temp>>16)&0xFF) + "/" + ((temp>>8)&0xFF) + "." + (temp&0xFF)) ;
														}
														break ;
				case TCPSERVERVOLTAGE				:	temp = ConvertVariableBufferToInt(buffer, bufferIndex, 4) ;
														bufferIndex += 4 ;
														testADIOSupplyDispLabel.setText((temp>>16) + "." + ((temp&0xFFFF)*10/65536) + " V") ;
														break ;
				case TCPSERVERWARNING				:	temp = ConvertVariableBufferToInt(buffer, bufferIndex, 4) ;
														bufferIndex += 4 ;
														stringTemp = "" ;
														if ((temp&0x01) == 0x01) {
															stringTemp += "Temp " ;
														}
														if ((temp&0x04) == 0x04) {
															stringTemp += "HighI " ;
														}
														if ((temp&0x10) == 0x10) {
															stringTemp += "LowV " ;
														}
														if ((temp&0x40) == 0x40) {
															stringTemp += "HighV " ;
														}
														if (stringTemp == "") {
															stringTemp = "None" ;
														}
														testADIOWarningDispLabel.setText(stringTemp) ;
														break ;
				case TCPSERVERCOMOPTIONS			:	bufferIndex += 4 ;
														break ;
				case TCPSERVERMAC					:	stringTemp = "" ;
														for (int i=0; i<6; i++)
															stringTemp += (Integer.toHexString((int)(buffer[bufferIndex+i]>>4)&0x0F)+Integer.toHexString((int)(buffer[bufferIndex+i])&0x0F)+" ") ;
														moduleParaMACValueLabel.setText(stringTemp) ;
														bufferIndex += 6 ;
														break ;
				case TCPSERVERIP					:	moduleParaIP1Text.setText(Integer.toString((int)buffer[bufferIndex++]&0xFF)) ;
														moduleParaIP2Text.setText(Integer.toString((int)buffer[bufferIndex++]&0xFF)) ;
														moduleParaIP3Text.setText(Integer.toString((int)buffer[bufferIndex++]&0xFF)) ;
														moduleParaIP4Text.setText(Integer.toString((int)buffer[bufferIndex++]&0xFF)) ;
														break ;
				case TCPSERVERSUBNETMASK			:	moduleParaSNM1Text.setText(Integer.toString((int)buffer[bufferIndex++]&0xFF)) ;
														moduleParaSNM2Text.setText(Integer.toString((int)buffer[bufferIndex++]&0xFF)) ;
														moduleParaSNM3Text.setText(Integer.toString((int)buffer[bufferIndex++]&0xFF)) ;
														moduleParaSNM4Text.setText(Integer.toString((int)buffer[bufferIndex++]&0xFF)) ;
														break ;
				case TCPSERVERTCPWATCHDOG			:	moduleParaWDText.setText(Integer.toString((int)buffer[bufferIndex]&0xFF)) ;
														bufferIndex += 1 ;
														break ;
				case TCPSERVERNAME					:	stringTemp = "" ;
														for (int i=0; i<16;i++)
															stringTemp += (char)buffer[bufferIndex+i] ;
														moduleParaNameText.setText(stringTemp) ;
														bufferIndex += 16 ;
														break ;
				case TCPSERVERUARTCONFIG			:	if ((buffer[bufferIndex]&0x80) == 0x80)
															moduleParaBpsChoice.select((buffer[bufferIndex++]&0x7F)+6) ;
														else
															moduleParaBpsChoice.select((buffer[bufferIndex++]&0x7F)) ;
														break ;
				case TCPSERVERI2CSPDCONFIG			:	switch (buffer[bufferIndex++]) {
															case 24 : moduleParaI2CSpeedChoice.select(1) ;
																	  break ;
															default : moduleParaI2CSpeedChoice.select(0) ;
																	  break ;
														}
														break;
				case TCPSERVERNUMBEROFUSERS			:	bufferIndex++ ;
														break ;
				case TCPSERVERINANALOGTHRESHOLD		:	temp = ConvertVariableBufferToInt(buffer, bufferIndex, 4) ;
														testADIOADThresholdText.setText((temp>>16) + "." + ((temp&0xFFFF)/6554)) ;
														bufferIndex += 4 ;
														break ;														
				case TCPSERVEROUTPUTS				:	if ((buffer[bufferIndex]&0x80) == 0x80)
															testADIOOutF.select("High") ;
														else
															testADIOOutF.select("Low") ;
														if ((buffer[bufferIndex]&0x40) == 0x40)
															testADIOOutE.select("High") ;
														else
															testADIOOutE.select("Low") ;
														if ((buffer[bufferIndex]&0x20) == 0x20)
															testADIOOutD.select("High") ;
														else
															testADIOOutD.select("Low") ;
														if ((buffer[bufferIndex]&0x10) == 0x10)
															testADIOOutC.select("High") ;
														else
															testADIOOutC.select("Low") ;
														if ((buffer[bufferIndex]&0x08) == 0x08)
															testADIOOutB.select("High") ;
														else
															testADIOOutB.select("Low") ;
														if ((buffer[bufferIndex]&0x04) == 0x04)
															testADIOOutA.select("High") ;
														else
															testADIOOutA.select("Low") ;
														if ((buffer[bufferIndex]&0x02) == 0x02)
															testADIOOut9.select("High") ;
														else
															testADIOOut9.select("Low") ;
														if ((buffer[bufferIndex]&0x01) == 0x01)
															testADIOOut8.select("High") ;
														else
															testADIOOut8.select("Low") ;
														if ((buffer[bufferIndex+1]&0x80) == 0x80)
															testADIOOut7.select("High") ;
														else
															testADIOOut7.select("Low") ;
														if ((buffer[bufferIndex+1]&0x40) == 0x40)
															testADIOOut6.select("High") ;
														else
															testADIOOut6.select("Low") ;
														if ((buffer[bufferIndex+1]&0x20) == 0x20)
															testADIOOut5.select("High") ;
														else
															testADIOOut5.select("Low") ;
														if ((buffer[bufferIndex+1]&0x10) == 0x10)
															testADIOOut4.select("High") ;
														else
															testADIOOut4.select("Low") ;
														if ((buffer[bufferIndex+1]&0x08) == 0x08)
															testADIOOut3.select("High") ;
														else
															testADIOOut3.select("Low") ;
														if ((buffer[bufferIndex+1]&0x04) == 0x04)
															testADIOOut2.select("High") ;
														else
															testADIOOut2.select("Low") ;
														if ((buffer[bufferIndex+1]&0x02) == 0x02)
															testADIOOut1.select("High") ;
														else
															testADIOOut1.select("Low") ;
														if ((buffer[bufferIndex+1]&0x01) == 0x01)
															testADIOOut0.select("High") ;
														else
															testADIOOut0.select("Low") ;
														bufferIndex += 2 ;
														break ;
				case TCPSERVERINPUTS				:	if ((buffer[bufferIndex]&0x80) == 0x80)
															testADIOInStateFLabel.setText("High") ;
														else
															testADIOInStateFLabel.setText("Low") ;
														if ((buffer[bufferIndex]&0x40) == 0x40)
															testADIOInStateELabel.setText("High") ;
														else
															testADIOInStateELabel.setText("Low") ;
														if ((buffer[bufferIndex]&0x20) == 0x20)
															testADIOInStateDLabel.setText("High") ;
														else
															testADIOInStateDLabel.setText("Low") ;
														if ((buffer[bufferIndex]&0x10) == 0x10)
															testADIOInStateCLabel.setText("High") ;
														else
															testADIOInStateCLabel.setText("Low") ;
														if ((buffer[bufferIndex]&0x08) == 0x08)
															testADIOInStateBLabel.setText("High") ;
														else
															testADIOInStateBLabel.setText("Low") ;
														if ((buffer[bufferIndex]&0x04) == 0x04)
															testADIOInStateALabel.setText("High") ;
														else
															testADIOInStateALabel.setText("Low") ;
														if ((buffer[bufferIndex]&0x02) == 0x02)
															testADIOInState9Label.setText("High") ;
														else
															testADIOInState9Label.setText("Low") ;
														if ((buffer[bufferIndex]&0x01) == 0x01)
															testADIOInState8Label.setText("High") ;
														else
															testADIOInState8Label.setText("Low") ;
														if ((buffer[bufferIndex+1]&0x80) == 0x80)
															testADIOInState7Label.setText("High") ;
														else
															testADIOInState7Label.setText("Low") ;
														if ((buffer[bufferIndex+1]&0x40) == 0x40)
															testADIOInState6Label.setText("High") ;
														else
															testADIOInState6Label.setText("Low") ;
														if ((buffer[bufferIndex+1]&0x20) == 0x20)
															testADIOInState5Label.setText("High") ;
														else
															testADIOInState5Label.setText("Low") ;
														if ((buffer[bufferIndex+1]&0x10) == 0x10)
															testADIOInState4Label.setText("High") ;
														else
															testADIOInState4Label.setText("Low") ;
														if ((buffer[bufferIndex+1]&0x08) == 0x08)
															testADIOInState3Label.setText("High") ;
														else
															testADIOInState3Label.setText("Low") ;
														if ((buffer[bufferIndex+1]&0x04) == 0x04)
															testADIOInState2Label.setText("High") ;
														else
															testADIOInState2Label.setText("Low") ;
														if ((buffer[bufferIndex+1]&0x02) == 0x02)
															testADIOInState1Label.setText("High") ;
														else
															testADIOInState1Label.setText("Low") ;
														if ((buffer[bufferIndex+1]&0x01) == 0x01)
															testADIOInState0Label.setText("High") ;
														else
															testADIOInState0Label.setText("Low") ;
														bufferIndex += 2 ;
														break ;
				case TCPSERVERAD0VALUE				:	temp = ConvertVariableBufferToInt(buffer, bufferIndex, 4) ;
														tempFloat = Math.round(((float)temp)/655.36);
														testADIOAD0ValueLabel.setText(tempFloat/100 + " V") ;
														bufferIndex += 4 ;
														break ;
				case TCPSERVERAD1VALUE				:	temp = ConvertVariableBufferToInt(buffer, bufferIndex, 4) ;
														tempFloat = Math.round(((float)temp)/655.36);
														testADIOAD1ValueLabel.setText(tempFloat/100 + " V") ;
														bufferIndex += 4 ;
														break ;
				case TCPSERVERAD2VALUE				:	temp = ConvertVariableBufferToInt(buffer, bufferIndex, 4) ;
														tempFloat = Math.round(((float)temp)/655.36);
														testADIOAD2ValueLabel.setText(tempFloat/100 + " V") ;
														bufferIndex += 4 ;
														break ;
				case TCPSERVERAD3VALUE				:	temp = ConvertVariableBufferToInt(buffer, bufferIndex, 4) ;
														tempFloat = Math.round(((float)temp)/655.36);
														testADIOAD3ValueLabel.setText(tempFloat/100 + " V") ;
														bufferIndex += 4 ;
														break ;
				case TCPSERVERAD4VALUE				:	temp = ConvertVariableBufferToInt(buffer, bufferIndex, 4) ;
														tempFloat = Math.round(((float)temp)/655.36);
														testADIOAD4ValueLabel.setText(tempFloat/100 + " V") ;
														bufferIndex += 4 ;
														break ;
				case TCPSERVERAD5VALUE				:	temp = ConvertVariableBufferToInt(buffer, bufferIndex, 4) ;
														tempFloat = Math.round(((float)temp)/655.36);
														testADIOAD5ValueLabel.setText(tempFloat/100 + " V") ;
														bufferIndex += 4 ;
														break ;
				case TCPSERVERAD6VALUE				:	temp = ConvertVariableBufferToInt(buffer, bufferIndex, 4) ;
														tempFloat = Math.round(((float)temp)/655.36);
														testADIOAD6ValueLabel.setText(tempFloat/100 + " V") ;
														bufferIndex += 4 ;
														break ;
				case TCPSERVERAD7VALUE				:	temp = ConvertVariableBufferToInt(buffer, bufferIndex, 4) ;
														tempFloat = Math.round(((float)temp)/655.36);
														testADIOAD7ValueLabel.setText(tempFloat/100 + " V") ;
														bufferIndex += 4 ;
														break ;
				case TCPSERVERAD8VALUE				:	temp = ConvertVariableBufferToInt(buffer, bufferIndex, 4) ;
														tempFloat = Math.round(((float)temp)/655.36);
														testADIOAD8ValueLabel.setText(tempFloat/100 + " V") ;
														bufferIndex += 4 ;
														break ;
				case TCPSERVERAD9VALUE				:	temp = ConvertVariableBufferToInt(buffer, bufferIndex, 4) ;
														tempFloat = Math.round(((float)temp)/655.36);
														testADIOAD9ValueLabel.setText(tempFloat/100 + " V") ;
														bufferIndex += 4 ;
														break ;
				case TCPSERVERADAVALUE				:	temp = ConvertVariableBufferToInt(buffer, bufferIndex, 4) ;
														tempFloat = Math.round(((float)temp)/655.36);
														testADIOADAValueLabel.setText(tempFloat/100 + " V") ;
														bufferIndex += 4 ;
														break ;
				case TCPSERVERADBVALUE				:	temp = ConvertVariableBufferToInt(buffer, bufferIndex, 4) ;
														tempFloat = Math.round(((float)temp)/655.36);
														testADIOADBValueLabel.setText(tempFloat/100 + " V") ;
														bufferIndex += 4 ;
														break ;
				case TCPSERVERADCVALUE				:	temp = ConvertVariableBufferToInt(buffer, bufferIndex, 4) ;
														tempFloat = Math.round(((float)temp)/655.36);
														testADIOADCValueLabel.setText(tempFloat/100 + " V") ;
														bufferIndex += 4 ;
														break ;
				case TCPSERVERADDVALUE				:	temp = ConvertVariableBufferToInt(buffer, bufferIndex, 4) ;
														tempFloat = Math.round(((float)temp)/655.36);
														testADIOADDValueLabel.setText(tempFloat/100 + " V") ;
														bufferIndex += 4 ;
														break ;
				case TCPSERVERADEVALUE				:	temp = ConvertVariableBufferToInt(buffer, bufferIndex, 4) ;
														tempFloat = Math.round(((float)temp)/655.36);
														testADIOADEValueLabel.setText(tempFloat/100 + " V") ;
														bufferIndex += 4 ;
														break ;
				case TCPSERVERADFVALUE				:	temp = ConvertVariableBufferToInt(buffer, bufferIndex, 4) ;
														tempFloat = Math.round(((float)temp)/655.36);
														testADIOADFValueLabel.setText(tempFloat/100 + " V") ;
														bufferIndex += 4 ;
														break ;
				default								:	break ;
			}
			cardsPanel.validate() ;
		}
	}
	
	// Display I2C data read from TCP 
	void DisplayI2CData(byte[] buffer, int bufferLength) {
		
		int bufferIndex	= 6;
		String temp ;
				
		bufferLength -= 2 ; // avoid read of checksum
		
		for (int i = bufferIndex; i<bufferLength; i++) {
			temp = ""+Integer.toHexString((int)buffer[i]&0xFF) ;
			temp = temp.toUpperCase() ;
			if(temp.length()==1)
				temp = "0"+temp ;
			testI2CDataRxTA.append(temp+" ") ;
		}
		testI2CDataRxTA.append("\r\n") ;
	}
	
	// Display I2C error read from TCP 
	void DisplayI2CError(byte[] buffer, int bufferLength) {
		
		switch (buffer[6]) {
			case 0x01 :	testI2CDataRxTA.append("Error! The I2C bus is not ready.\r\n") ;
						break ;
			case 0x02 :	testI2CDataRxTA.append("Error! A timeout occurred when waiting answer from the I2C slave.\r\n") ;
						break ;
			case 0x03 :	testI2CDataRxTA.append("Error! 2X + 3Y > 240 in I2CRWwithAck received.\r\n") ;
						break ;
			default :	testI2CDataRxTA.append("Unknown I2C error!\r\n") ;
						break ;
		}
	}
	
	// Display I2C valid addresses read from TCP
	void DisplayI2CScanData(byte[] buffer, int bufferLength) {
		
		int bufferIndex	= 6;
		String temp ;
				
		bufferLength -= 2 ;
		testI2CScanAnswerList.removeAll();
		
		for (int i = bufferIndex; i<bufferLength; i++) {
			temp = Integer.toHexString(buffer[i]) ;
			if(temp.length()==1)
				temp = "0"+temp ;
			testI2CScanAnswerList.addItem(temp) ;
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	// Misc functions
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	// buffer to int conversion
	int ConvertVariableBufferToInt(byte[] buffer, int bufferIndex, int variableLength) {
		
		int temp = 0 ;

   		switch (variableLength) {
  			case 4 : temp   = ((int)(buffer[bufferIndex++])<<24)&0xFF000000 ;
   			case 3 : temp  += ((int)(buffer[bufferIndex++])<<16)&0xFF0000 ;
   			case 2 : temp  += ((int)(buffer[bufferIndex++])<<8)&0xFF00 ;
   			case 1 : temp  += ((int)(buffer[bufferIndex++]))&0xFF ;		
   		}
   		
   		return temp ;
	}

	// Build Constrains for GridBag Layout
	void buildConstraints(GridBagConstraints gbc, int gx, int gy, int gw, int gh, int wx, int wy, int fillStyle, int anchorStyle) {
		gbc.gridx = gx ;
		gbc.gridy = gy ;
		gbc.gridwidth = gw ;
		gbc.gridheight = gh ;
		gbc.weightx = wx ;
		gbc.weighty = wy ;
		gbc.fill = fillStyle ;
		gbc.anchor = anchorStyle ;
	}
	

	// Checksum calculation
	int calcChecksum(byte[] ByteStream, int lenght)
	{
		int checksum = 0 ;
		int j = 0 ;
		int temp = 0 ;
		
	
		for(int i=lenght; i>0; i-=2)
		{
			if (i>1) {
                temp = ((ByteStream[j]<<8)&0xFF00)+((ByteStream[j+1])&0xFF);
                j+=2;
                checksum += (~temp)&0xFFFF;
			} else {
                checksum += (~((ByteStream[j++]<<8)&0xFF00))&0xFFFF ;
			}
		}
        checksum = ((checksum & 0xFFFF0000)>>16) + (checksum & 0xFFFF) ;
        checksum = ((checksum & 0xFFFF0000)>>16) + (checksum & 0xFFFF) ;
		return checksum;
	}
	
	// Read ASCII hex representation and convert to bytes
	int ReadI2CData2Write(byte[] Buffer, int BufferIndex) {
		        
        String AsciiBuf = testI2CHex2WriteText.getText() ;

		while(AsciiBuf.indexOf(" ")!=-1)
		{
        	
        	System.err.println(""+AsciiBuf.indexOf(" ")) ;
		    Buffer[BufferIndex] = (byte)(Integer.valueOf(AsciiBuf.substring(0,AsciiBuf.indexOf(" ")),16).intValue()&0xff) ;
		    AsciiBuf=AsciiBuf.substring(AsciiBuf.indexOf(" ")+1,AsciiBuf.length());
		    BufferIndex++ ;
		}
		   
		if (AsciiBuf.length()>0)
        	Buffer[BufferIndex++] = (byte)(Integer.valueOf(AsciiBuf.substring(0,2),16).intValue()&0xff) ;

		return BufferIndex-2 ;
	}
}

	/////////////////////////////////////////////////////////////////////////////////////////////
	// Other classes
	/////////////////////////////////////////////////////////////////////////////////////////////
	
// FMod-TCP parameters object construct
class ModuleVariable extends Object {
	
	boolean	writeEnable = false ;
	boolean readEnable = false ;
	final int valuesLength = 16 ;
	byte[] values = new byte[valuesLength+1] ;
	int length = 0 ;
	
	ModuleVariable(int address, int variableLength) {
		
		values[0] = (byte)address ;
		length = variableLength ;
			
	}	

	// Function to write a value into byte array from int or float
	void setIntValue(int intValue) {
		switch (length) {
    		case 4  : values[valuesLength-3]  = (byte)((intValue>>24)&0xFF) ;
    		case 3  : values[valuesLength-2]  = (byte)((intValue>>16)&0xFF) ;
    		case 2  : values[valuesLength-1]  = (byte)((intValue>>8)&0xFF) ;
    		case 1  : values[valuesLength-0]  = (byte)(intValue&0xFF) ;
    		default : break ;
    	}
	}
	
	void setFloatValue(float floatValue) {
		setIntValue(Float.floatToIntBits(floatValue)) ;
	}


	// Functions to test if write or read is necessary and copy value to buffer if yes
	
	int TestWriteAndFillBuffer(byte[] buffer, int bufferIndex) {
		if ((writeEnable) && (bufferIndex+length+1<180)) {
			buffer[bufferIndex++]  = values[0] ;
			
			for (int i=valuesLength+1-length; i<valuesLength+1; i++) {
				buffer[bufferIndex++] = values[i] ;
			}
			writeEnable = false ;
		}	
		return 	bufferIndex ;
	}
	
	int[] TestReadAndFillBuffer(byte[] buffer, int[] bufferIndex) {
		if ((readEnable) && (bufferIndex[0]<180) && (bufferIndex[1]+1+length<180)) {
			buffer[bufferIndex[0]++] = values[0] ;
			bufferIndex[1] += 1+length ;
			readEnable = false ;
		}	
		return 	bufferIndex ;		
	}
}