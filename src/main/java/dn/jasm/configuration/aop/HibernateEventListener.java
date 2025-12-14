package dn.jasm.configuration.aop;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.event.spi.LoadEvent;
import org.hibernate.event.spi.LoadEventListener;

@Slf4j
public class HibernateEventListener implements LoadEventListener {
    @Override
    public void onLoad(LoadEvent event, LoadType loadType) throws HibernateException {

    }
}