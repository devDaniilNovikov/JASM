package dn.jasm.service.impl;


import dn.jasm.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
@Slf4j
public class LogServiceImpl implements LogService {



    @Override
    public void cacheLog(String key, Object element) {
        log.info("[Value: {} with key: {} will get from cache]",element,key);
    }

    @Override
    public void cacheLog(Object element) {
        log.info("[Value: {} will get from cache]",element);
    }

    @Override
    public void cacheLog(Object... elements) {
        log.info("[Values: {} will get from cache]",elements);

    }

    @Override
    public void dbLog(Object... elements) {
        log.info("[Values: {} will get from db]",elements);

    }

    @Override
    public void dbLog(String id, Object object) {
        log.info("[Value: {} with id: {} will get from db]",object,id);

    }

    @Override
    public void dbLog(Object object) {
        log.info("[Value: {}  will get from db]",object);

    }
}
