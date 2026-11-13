package com.amirbahadoramiri.quickmail;

public interface QuickMailListener {

    void onSuccess();
    void onFailure(Throwable t);

}
