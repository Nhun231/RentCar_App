package com.anhbhn.rentcar.data.dto.response.booking;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class WalletResponse implements Serializable {
    @SerializedName("id")
    public String id;
    
    @SerializedName("balance")
    public long balance;
}

