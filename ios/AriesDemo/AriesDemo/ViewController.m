/*
* Copyright SecureKey Technologies Inc. All Rights Reserved.
* SPDX-License-Identifier: Apache-2.0
*/

#import "ViewController.h"

@interface ViewController ()

@property IBOutlet UISwitch *agentTypeSwitch;
@property IBOutlet UITextField *agentURLTextField;
@property IBOutlet UIButton *getCredentialsButton;
@property IBOutlet UITextView *getCredentialsResponseTextView;

@property NSString *urlToUse;
@property BOOL useLocalAgent;
@property ApiAriesController* ariesAgent;

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];

    [self addAgentLabel];
    [self addAgentSiwtch];
    [self addAgentURLTextField];
    [self addCreateAgentButton];
    [self addGetCredentialsButton];
    [self addResponseLabel];
    [self addGetCredentialsResponse];
}

- (void) addAgentLabel {
    UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(10, 10, 250, 160)];
    [label setText:@"Use Local Agent: "];
    [self.view addSubview:label];
}

- (void) addAgentSiwtch {
    _agentTypeSwitch = [[UISwitch alloc] init];
    [self.view addSubview:_agentTypeSwitch];
    _agentTypeSwitch.center = CGPointMake(180, 90);
    [_agentTypeSwitch addTarget:self action:@selector(switched:)
    forControlEvents:UIControlEventValueChanged];
}

- (IBAction) switched: (id)sender {
    _ariesAgent = nil;
    _useLocalAgent = _agentTypeSwitch.on ? true : false;
    _agentURLTextField.hidden = _useLocalAgent;
    [_getCredentialsButton setBackgroundColor:UIColor.grayColor];
    [_getCredentialsButton setEnabled:false];
    [_getCredentialsResponseTextView setText:@""];
}

- (void)addAgentURLTextField {
    UILabel *prefixLabel = [[UILabel alloc]initWithFrame:CGRectZero];
    prefixLabel.text =@"  Agent URL";
    [prefixLabel setFont:[UIFont boldSystemFontOfSize:14]];
    [prefixLabel sizeToFit];

    _agentURLTextField = [[UITextField  alloc] initWithFrame:
    CGRectMake(50, 150, 280, 30)];

    _agentURLTextField.borderStyle = UITextBorderStyleRoundedRect;
    _agentURLTextField.contentVerticalAlignment = UIControlContentVerticalAlignmentCenter;
    [_agentURLTextField setFont:[UIFont boldSystemFontOfSize:12]];
    _agentURLTextField.placeholder = @"http://your.agent.url";
    _agentURLTextField.leftView = prefixLabel;
    _agentURLTextField.leftViewMode = UITextFieldViewModeAlways;
    _agentURLTextField.autocapitalizationType = UITextAutocapitalizationTypeNone;

    [self.view addSubview:_agentURLTextField];
    _agentURLTextField.delegate = self;

    [_agentURLTextField addTarget:self action:@selector(textChanged:) forControlEvents:UIControlEventEditingChanged];
}

- (void) textChanged: (UITextField*)textField {
    _urlToUse = textField.text;
}

- (void) addCreateAgentButton {
    UIButton *button = [[UIButton alloc] init];
    [button setFrame:CGRectMake(90, 200, 200, 40)];
    [button setTintColor:UIColor.whiteColor];
    [button setBackgroundColor:UIColor.blueColor];
    
    [button setTitle:@"Create Agent" forState:UIControlStateNormal];
    [self.view addSubview:button];
    [button addTarget:self action:@selector(setAgent:) forControlEvents:UIControlEventTouchUpInside];
}

- (void) setAgent: (UIButton*)button {
    ConfigOptions *opts = ConfigNew();
    [opts setAgentURL:_urlToUse];
    [opts setUseLocalAgent:_useLocalAgent];
    
    NSError *error = nil;
    
    _ariesAgent = (ApiAriesController*) AriesagentNew(opts, &error);
    if(error) {
        NSLog(@"error creating an aries agent: %@", error);
    } else {
        [_getCredentialsButton setBackgroundColor:UIColor.purpleColor];
        [_getCredentialsButton setEnabled:true];
    }
}

- (NSString*) getCredentials: (ApiAriesController*) agent
                   withError: (NSError*) error {

    ApiVerifiableController *ic = (ApiVerifiableController*) [agent getVerifiableController:&error];
    if(error) {
        NSLog(@"error creating an verifiable controller instance: %@", error);
    }

    NSString *credResp = @"";
    NSData *data = [@"" dataUsingEncoding:NSUTF8StringEncoding];
    
    ModelsRequestEnvelope *req = ModelsNewRequestEnvelope(data);
    ModelsResponseEnvelope *resp = [ic getCredentials:req];
    if(resp.error) {
        NSLog(@"error getting credentials: %@", resp.error.message);
    }
    else {
        credResp = [[NSString alloc] initWithData:resp.payload encoding:NSUTF8StringEncoding];
        NSLog(@"get credentials response: %@", credResp);
    }
    
    return credResp;
}

- (void) setCredentialsResponse: (UIButton*)button {
    NSError *error = nil;
    NSString *credResp = [self getCredentials:_ariesAgent withError:error];
    
    [_getCredentialsResponseTextView setText:credResp];
}

- (void) addGetCredentialsButton {
    _getCredentialsButton = [[UIButton alloc] init];
    [_getCredentialsButton setFrame:CGRectMake(90, 270, 200, 40)];
    [_getCredentialsButton setBackgroundColor:UIColor.grayColor];
    
    [_getCredentialsButton setTitle:@"Get Credentials" forState:UIControlStateNormal];
    [self.view addSubview:_getCredentialsButton];
    
    [_getCredentialsButton setEnabled:false];
    [_getCredentialsButton addTarget:self action:@selector(setCredentialsResponse:) forControlEvents:UIControlEventTouchUpInside];
}

- (void) addResponseLabel {
    UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(100, 280, 250, 160)];
    [label setText:@"Retrieved Credentials: "];
    [self.view addSubview:label];
}

- (void) addGetCredentialsResponse {
    _getCredentialsResponseTextView = [[UITextView alloc]
                                       initWithFrame:CGRectMake(5, 380,
                                                                CGRectGetWidth(self.view.bounds), CGRectGetHeight(self.view.bounds))];
    _getCredentialsResponseTextView.textContainer.lineBreakMode = NSLineBreakByWordWrapping;
    _getCredentialsResponseTextView.editable = NO;
    [self.view addSubview:_getCredentialsResponseTextView];
}

@end
