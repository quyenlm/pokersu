package com.mrmq.poker.db;

import org.hibernate.cfg.Configuration;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
    }
}
