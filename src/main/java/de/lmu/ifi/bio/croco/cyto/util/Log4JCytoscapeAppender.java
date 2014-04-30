package de.lmu.ifi.bio.croco.cyto.util;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log4JCytoscapeAppender extends AppenderSkeleton {
	private Logger logger;
	public Log4JCytoscapeAppender(){
		logger = LoggerFactory.getLogger(getClass());
		
	}
	
	@Override
	public void close() {	}

	@Override
	public boolean requiresLayout() {
		return true;
	}

	@Override
	protected void append(LoggingEvent event) {
		//we pass all log events to slf4j as info event
		logger.info(super.getLayout().format(event).replace("\n", ""));
	}

}
