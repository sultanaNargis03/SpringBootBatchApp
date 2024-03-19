package com.telusko.config;

import org.springframework.batch.item.ItemProcessor;

import com.telusko.binding.Customer;

public class CustomerProcessor implements ItemProcessor<Customer, Customer>
{
	
	public Customer process(Customer item)
	{
		//logic to process/filter/perform operation
		System.out.println("processor");
			return item;
		
	}

}

