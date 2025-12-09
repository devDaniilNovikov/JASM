package dn.jasm.service;

public interface LogService {

    void cacheLog(String key,
                  Object element);

    void cacheLog(Object element);

    void cacheLog(Object... elements);

    void dbLog(Object... objects);

    void dbLog(String id,
               Object object);

    void dbLog(Object object);




}
