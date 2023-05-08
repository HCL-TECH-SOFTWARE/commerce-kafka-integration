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
const routes: Routes = [
    // add new route for integrations
    {
		path: "integrations",
		loadChildren: () =>
			import("./features/integration/integrations.module").then(
				(m) => m.IntegrationsModule
			)
	},
]

@NgModule({
	imports: [
		RouterModule.forRoot(routes, { relativeLinkResolution: "legacy" })
	],
	exports: [RouterModule]
})
export class AppRoutingModule {}