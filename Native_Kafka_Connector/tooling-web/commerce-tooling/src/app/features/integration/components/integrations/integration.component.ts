/*
 *-------------------------------------------------------------------
 * Licensed Materials - Property of HCL Technologies
 *
 * HCL Commerce
 *
 * (C) Copyright HCL Technologies Limited 1996, 2020

 *-------------------------------------------------------------------
 */

import { Component, OnInit, ElementRef, Input, SystemJsNgModuleLoader } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { FormGroup, FormControl, FormBuilder, Validators } from "@angular/forms";
import { IntegrationsMainService } from "../../services/integrations-main.service";

interface ResponseInterface {
	name: string;
	storeId: number;
	value: any;
}

@Component({
	templateUrl: "./integration.component.html",
	styleUrls: ["./integration.component.scss"]
})
export class integrationsComponent implements OnInit {
	kafkaFromGroup: FormGroup;
	consumersFormGroup: FormGroup;
	producersFromGroup: FormGroup;
	configrationFormGroup: FormGroup;
	consumerEditFormGroup: FormGroup;
	consumersConfigArr = [];
	producersConfigArr = [];
	consumerEditConfigArr = [];
	configrationControlObj = {};
	configrationArr  = [];
	singleRequestData = [];
	showLoader = false;
	displayConsumerForm = false;
	consumerEditKey: string;
	consumerEditControlObj = {};
	singleRequestConsumerData = [];
	ipAddressRegex = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
	hostnameRegex = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9])$";
	hostRegex = this.ipAddressRegex + "|" + this.hostnameRegex;
	portRegex = "^([1-9][0-9]{0,3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$";
	usernameRegex =  "^[a-z0-9_.]+$";

	constructor(
			private _formBuilder: FormBuilder,
			private translateService: TranslateService,
			private _integrationsMainService: IntegrationsMainService
	) { }

	ngOnInit(): void {
		this.createKafkaForm();
		this.updateKafkaData();
		setTimeout(() => {
			this.kafkaFromGroup.markAsPristine();
			this.kafkaFromGroup.markAsUntouched();
		}, 200);
	}

	public reset() {
		this.consumersConfigArr = [];
		this.producersConfigArr = [];
		this.configrationControlObj = {};
		this.singleRequestData = [];
		this.displayConsumerForm = false;
		this.consumerEditControlObj = {};
	}

	public showConsumerForm(consumerKey){
		this.consumerEditKey = consumerKey;
		this.singleRequestConsumerData = [];
		this.displayConsumerForm = true;
		let consumerFormKey = "kafka.consumer." + consumerKey;
		let consumerEditFormData = {};
		this.consumerEditFormGroup.reset();

		for (const key in this.consumerEditControlObj) {
			if (this.consumerEditControlObj.hasOwnProperty(key)) {
				switch (key) {
					case consumerFormKey + ".host":
						consumerEditFormData["host"] = this.consumerEditControlObj[key];
						break;
					case consumerFormKey + ".port":
						consumerEditFormData["port"] = this.consumerEditControlObj[key];
						break;
					case consumerFormKey + ".username":
						consumerEditFormData["username"] = this.consumerEditControlObj[key];
						break;
					case consumerFormKey + ".password":
						consumerEditFormData["password"] = this.consumerEditControlObj[key];
						break;
					default:
						break;
				}
			}
		}
		this.consumerEditFormGroup.patchValue(consumerEditFormData);
	}
	public closeConsumerForm(){
		this.displayConsumerForm = false;
	}
	public updateKafkaData(){
		this.reset();
		this._integrationsMainService.getKafkaConfigration().subscribe((items: ResponseInterface[]) =>{
			this.setKafkaConfig(items);
		});
	}

	public setKafkaConfig(items: ResponseInterface[]) {
		items.forEach(({name, value: valueItem, storeId}: any) => {
			let tempObj = {};
			let value = valueItem.toLowerCase() === "true" ? true : false;
			tempObj["storeId"] = storeId;
			if (name.includes("kafka.producer")){
				let key = name.replace("kafka.producer.", "");
				tempObj[key] = value;
				this.producersConfigArr.push(tempObj);
				this.producersFromGroup.addControl(key, new FormControl(value));
			}

			if (name.includes("kafka.consumer.") && (!name.includes(".host") && !name.includes(".port") && !name.includes(".username") && !name.includes(".password"))) {
				let key = name.replace("kafka.consumer.", "");
				tempObj[key] = value;
				this.consumerEditControlObj[key] = value;
				this.consumersConfigArr.push(tempObj);
				this.consumersFormGroup.addControl(key, new FormControl(value));
			}

			if (name.includes("kafka.consumer.")) {
				let key = name;
				tempObj[key] = valueItem;
				this.consumerEditControlObj[key] = valueItem;
				this.consumerEditConfigArr.push(tempObj);
			}
			if (name.includes("kafka.port") || name.includes("kafka.host") || name.includes("kafka.username") || name.includes("kafka.password")) {
				let key = name.replace("kafka.", "");
				tempObj[key] = valueItem;
				this.configrationArr.push(tempObj);
				switch (key) {
				case "port":
					this.configrationControlObj["port"] = valueItem;
					break;
				case "host":
					this.configrationControlObj["host"] = valueItem;
					break;
				case "username":
					this.configrationControlObj["username"] = valueItem;
					break;
				case "password":
					this.configrationControlObj["password"] = valueItem;
					break;
				default:
					break;
				}	
			}
		});
		this.configrationFormGroup.patchValue(this.configrationControlObj);

	}

	public createKafkaForm () {
		this.consumersFormGroup = this._formBuilder.group({});
		this.producersFromGroup = this._formBuilder.group({});

		this.configrationFormGroup = this._formBuilder.group({
			host: ["", [Validators.required]],
			port: ["", [Validators.required, Validators.pattern(this.portRegex)]],
			username: ["", Validators.pattern(this.usernameRegex)],
			password: [""]
		});


		this.consumerEditFormGroup = this._formBuilder.group({
			host: ["", [Validators.required]],
			port: ["", [Validators.required, Validators.pattern(this.portRegex)]],
			username: ["", Validators.pattern(this.usernameRegex)],
			password: [""]
		});

		this.kafkaFromGroup = this._formBuilder.group({
			configration: this.configrationFormGroup,
			consumers: this.consumersFormGroup,
			producers: this.producersFromGroup
			
		});
	}

	public updateConfigration(value, key, arrayType?: string) {
		let param = {};
		let body = {};
		let config: any = {};
		let consumerRequestBody = {};

		if (arrayType === "consumers" || arrayType === "producers") {
			if (arrayType === "consumers") {
				config = this.consumersConfigArr.filter(item => item.hasOwnProperty(key))[0];
				param = {
					storeId : config.storeId,
					name: `kafka.consumer.${key}`
				};

			} else {
				config = this.producersConfigArr.filter(item => item.hasOwnProperty(key))[0];
				param = {
					storeId : config.storeId,
					name: `kafka.producer.${key}`
				};
			}
		} else if (arrayType === "consumerEdit"){
			key = `kafka.consumer.${this.consumerEditKey}.${key}`;
			config = this.consumerEditConfigArr.filter(item => item.hasOwnProperty(key))[0] ?? 0;
			param = {
				storeId : config.storeId ?? 0,
				name: key
			};
		} 
		else {
			config = this.configrationArr.filter(item => item.hasOwnProperty(key))[0] ?? 0;
			param = {
				storeId : config.storeId ?? 0,
				name: `kafka.${key}`
			};
		}

		if(arrayType === "consumerEdit"){
			consumerRequestBody = { ...param, value};
			let index = this.singleRequestConsumerData.findIndex(item => item.key === key);
			if (index === -1) {
				this.singleRequestConsumerData.push({key, consumerRequestBody, param});
			} else {
				this.singleRequestConsumerData[index] = {key, consumerRequestBody, param};
			}
		}else{
			body = { ...param, value};
			let index = this.singleRequestData.findIndex(item => item.key === key);
			if (index === -1) {
				this.singleRequestData.push({key, body, param});
			} else {
				this.singleRequestData[index] = {key, body, param};
			}
		}
	}

	public startLoader() {
		this.showLoader = true;
		setTimeout(() => {
			this.showLoader = false;
			this.displayConsumerForm = false;
			this.updateKafkaData();
		}, 800);
	}

	public refreshPage() {
		document.getElementById("restButton").click();
	}

	public cancel(stepper) {
		this.showLoader = true;
		this.consumersConfigArr.forEach(item => {
			let key = Object.keys(item).filter(item => item !== "storeId")[0];
			this.consumersFormGroup.removeControl(key);
		});
		this.producersConfigArr.forEach(item => {
			let key = Object.keys(item).filter(item => item !== "storeId")[0];
			this.producersFromGroup.removeControl(key);
		});
		this.kafkaFromGroup.reset();
		this.updateKafkaData();
		this.startLoader();
	}

	public submit(formName) {
		this.showLoader = true;
		if(formName === "configrationFormGroup"){
			this.singleRequestData.forEach(reqData => {
				this.showLoader = true;
				if (!this.configrationControlObj.hasOwnProperty(reqData.key) &&
					(reqData.key === "port" || reqData.key === "host" || reqData.key === "username" || reqData.key === "password")) {	
					this._integrationsMainService.createKafkaConfigration(reqData.body).subscribe(res => {
						// debugger;
					});
				} else {
					this._integrationsMainService.updateKafkaConfigration(reqData.body, reqData.param).subscribe(res => {
						// debugger;
	
					});
				}
			});
			this.startLoader();
		}else{
			this.singleRequestConsumerData.forEach(reqData => {
				this.showLoader = true;
				if (!this.consumerEditControlObj.hasOwnProperty(reqData.key)) {
					this._integrationsMainService.createKafkaConfigration(reqData.consumerRequestBody).subscribe(res => {
						// debugger;
					});
				} else {
					this._integrationsMainService.updateKafkaConfigration(reqData.consumerRequestBody, reqData.param).subscribe(res => {
						// debugger;
	
					});
				}
			});
			this.startLoader();
		}
		
	}
}
