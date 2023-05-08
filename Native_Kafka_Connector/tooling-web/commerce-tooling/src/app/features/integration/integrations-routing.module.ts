/*
 *-------------------------------------------------------------------
 * Licensed Materials - Property of HCL Technologies
 *
 * HCL Commerce
 *
 * (C) Copyright HCL Technologies Limited 1996, 2020

 *-------------------------------------------------------------------
 */

import { NgModule } from "@angular/core";
import { Routes, RouterModule } from "@angular/router";
import { integrationsComponent } from "./components/integrations/integration.component";

const routes: Routes = [
	{
		path: "integrations", component: integrationsComponent
	},
	{
		path: "", redirectTo: "integrations", pathMatch: "full"
	}
];

@NgModule({
	imports: [
		RouterModule.forChild(routes)
	],
	exports: [RouterModule]
})
export class IntegrationsRoutingModule { }
