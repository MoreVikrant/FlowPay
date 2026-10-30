package com.FlowPay.merchant;

import com.FlowPay.common.ApiException;
import com.FlowPay.common.Ids;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Set;

@Service
public class MerchantService {

    private final MerchantRepository repo;
    private final Set<String> allowedHosts;

    public MerchantService(MerchantRepository repo, @Value("${app.webhook-hosts}") String hosts) {
        this.repo = repo;
        this.allowedHosts = Set.of(hosts.split(","));
    }

    // if we are giving the Api key then we have to make the hash right ?
    public static String hash(String value){

        try{ // we will make the hash like this
            return HexFormat.of()
                    .formatHex(MessageDigest.getInstance("SHA-256")
                            .digest(value.getBytes(StandardCharsets.UTF_8)));

        }
        catch (NoSuchAlgorithmException e){
            throw  new IllegalStateException(e);
        }
    }

    @Transactional // because entry in the database
    public MerchantDtos.Registered register(MerchantDtos.Register r){
        Merchant m = new Merchant();
        m.id=Ids.next("mer");
        m.name= r.name();
        m.email=r.email();
        String key = Ids.next("fp_test");
        m.apiKeyHash=hash(key);
        m.webhookSecret=Ids.next("whsec");
        repo.save(m);
        return new MerchantDtos.Registered(m, key, m.webhookSecret);
    }

    public Merchant authenticate(String key){          // we are authenticating the key
        if(key == null || key.isBlank()){
            throw new ApiException(HttpStatus.UNAUTHORIZED,"A valid X-Api-key is required");
        }else{
            return repo.findByApiKeyHash(hash(key))
                    .orElseThrow(()->  new ApiException(HttpStatus.UNAUTHORIZED,"Invalid API key"));
        }
    }

    public Merchant lock(String id){
        return repo.lockById(id).orElseThrow(ApiException::missing);
    }

    @Transactional
    public Merchant update(String id,MerchantDtos.Update r){

        Merchant m= lock(id);
        validate(r.webhookUrl());
        m.name=r.name();
        m.email=r.email();
        m.webhookUrl=r.webhookUrl();
        return m;

    }

    // if i am updating the merchant info or entering the details then we will check the validation
    public void validate(String value){  //checks whether the caller has supplied a valid API key.

        if(value == null || value.isBlank()) return ;

       try {
            URI u = URI.create(value);   // this is form to create the url and without this can not validate the url
            if (!Set.of("http", "https").contains(u.getScheme())
                    || !allowedHosts.contains(u.getHost())
                    || u.getUserInfo() != null
                    || u.getFragment() != null
            ) {
                throw new IllegalStateException();
            }
        }
        catch(Exception e){                                                  // written in application.properties
           throw  ApiException.bad("Webhook url must be in HTTP(s) and host in Webhook_hosts");
        }


    }
}
