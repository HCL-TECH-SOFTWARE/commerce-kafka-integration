package com.hcl.commerce.rest.order.postprocessor.commands;

import com.ibm.commerce.command.ControllerCommand;
import com.ibm.commerce.foundation.config.extensions.annotations.ResourceHandlerPostprocessAnnotation;

/**
 * @author hcladmin
 *
 */
@ResourceHandlerPostprocessAnnotation(
path="cart",
method="POST")
public interface OrderItemAddPostprocessorCmd extends ControllerCommand{
	String defaultCommandClassName = 
		     "com.hcl.commerce.rest.order.postprocessor.commands.OrderItemAddPostprocessorCmdImpl";

}
