/*
*==================================================
Copyright [2022] [HCL America, Inc.]
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*==================================================
*/

import { Injectable } from "@angular/core";
import { Observer, Observable } from "rxjs";
import { ConnectionSpecsService } from "../../../rest/services/connection-specs.service";
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders } from "@angular/common/http";
import { BaseService as __BaseService } from "../../../rest/base-service";
import { ApiConfiguration as __Configuration } from "../../../rest/api-configuration";
import { StrictHttpResponse as __StrictHttpResponse } from "../../../rest/strict-http-response";

import { map as __map, filter as __filter } from "rxjs/operators";

interface kafkaInterface {
	storeId?: string,
	name?: number,
	value?: string,
	offset?: number;
	limit?: number;
	fields?: string;
	expand?: string;
	StoreConfiguration?: Object;
}

@Injectable({
	providedIn: "root"
})
export class IntegrationsMainService extends __BaseService {
	readonly kafkaConfigurationsPath = "/rest/admin/v2/store-configurations";
  	// readonly createStoreConfigurationsPath = "/rest/admin/v2/store-configurations";
  	// readonly getStoreTransportByIdPath = "/rest/admin/v2/store-configurations/storeId:{storeId},name:{transportId}";
  	// readonly deleteStoreTransportByIdPath = "/rest/admin/v2/store-configurations/storeId:{storeId},name:{transportId}";
  	// readonly patchStoreTransportByIdPath = "/rest/admin/v2/store-configurations/storeId:{storeId},name:{transportId}";

	// properties: any = null;
	// currentTransportId: number = null;
	// currentStoreId: number = null;
	processing = false;

	// private currentConnectionSpecs: any = null;

	constructor(
		private connectionSpecsService: ConnectionSpecsService,
		config: __Configuration,
		http: HttpClient
	) {
		super(config, http);
	}

	getKafkaConfigration(): Observable<any> {
		this.processing = true;
		return new Observable<Array<any>>((observer: Observer<Array<any>>) => {
			this.KafkaReqResConfiguration("GET", this.kafkaConfigurationsPath).pipe(
				__map(_r => _r.body as null),
				__map((_r: any) => _r.items as null)
			).subscribe((items: any[]) => {
				observer.next(items.filter(item => item.name.includes("kafka")));
			},
			error => {
				this.processing = false;
				observer.error(error);
			});
		});
	}

	updateKafkaConfigration(body, param?: any): Observable<any> {
		this.processing = true;
		return new Observable<Array<any>>((observer: Observer<Array<any>>) => {
			this.KafkaReqResConfiguration("PATCH", this.kafkaConfigurationsPath, param, body).pipe(
				__map(_r => _r.body as null)
			).subscribe((res: any) => {
				observer.next(res);
			},
			error => {
				this.processing = false;
				observer.error(error);
			});
		});
	}

	createKafkaConfigration(body, param?: any): Observable<any> {
		this.processing = true;
		return new Observable<Array<any>>((observer: Observer<Array<any>>) => {
			this.KafkaReqResConfiguration("POST", this.kafkaConfigurationsPath, param, body).pipe(
				__map(_r => _r.body as null)
			).subscribe((res: any) => {
				observer.next(res);
			},
			error => {
				this.processing = false;
				observer.error(error);
			});
		});
	}

	KafkaReqResConfiguration(reqType, url, params?: kafkaInterface, body?: any): Observable<__StrictHttpResponse<null>> {
		let __headers = new HttpHeaders();
		let __body: any = null;

		if (body) {
			__body = body;
		}

		if (params) {
			url = `${url}/name:${params.name},storeId:${params.storeId}`;
		}

		let req = new HttpRequest<any>(
			reqType,
			this.rootUrl + url,
			__body,
			{
				headers: __headers,
				responseType: "json"
			});

		return this.http.request<any>(req).pipe(
			__filter(_r => _r instanceof HttpResponse),
			__map((_r) => {
				return _r as __StrictHttpResponse<null>;
			})
		);
	}
}
