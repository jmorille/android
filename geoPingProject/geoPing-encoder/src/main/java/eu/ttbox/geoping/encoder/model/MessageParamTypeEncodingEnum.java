package eu.ttbox.geoping.encoder.model;


import eu.ttbox.geoping.encoder.params.IParamEncoder;
import eu.ttbox.geoping.encoder.params.encoder.DateInSecondeParamEncoder;
import eu.ttbox.geoping.encoder.params.encoder.GpsProviderParamEncoder;
import eu.ttbox.geoping.encoder.params.encoder.IntegerParamEncoder;
import eu.ttbox.geoping.encoder.params.encoder.LongParamEncoder;
import eu.ttbox.geoping.encoder.params.encoder.MultiFieldParamEncoder;
import eu.ttbox.geoping.encoder.params.encoder.StringParamEncoder;
import eu.ttbox.geoping.encoder.params.helper.IntegerEncoded;
import eu.ttbox.geoping.encoder.params.helper.LongEncoded;

public class MessageParamTypeEncodingEnum {

    public static final IParamEncoder STRING = new StringParamEncoder();
    public static final IParamEncoder GPS_PROVIDER = new GpsProviderParamEncoder();
    public static final IParamEncoder STRING_BASE64 = null ;
    public static final IParamEncoder INT = new IntegerParamEncoder();
    public static final IParamEncoder LONG = new LongParamEncoder();
    public static final IParamEncoder DATE = new DateInSecondeParamEncoder();
    public static final IParamEncoder MULTI = new MultiFieldParamEncoder();

}
