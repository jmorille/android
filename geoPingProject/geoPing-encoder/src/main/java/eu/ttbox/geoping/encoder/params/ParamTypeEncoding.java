package eu.ttbox.geoping.encoder.params;


import eu.ttbox.geoping.encoder.params.encoding.DateInSecondeParamEncoder;
import eu.ttbox.geoping.encoder.params.encoding.GpsProviderParamEncoder;
import eu.ttbox.geoping.encoder.params.encoding.IntegerParamEncoder;
import eu.ttbox.geoping.encoder.params.encoding.LongParamEncoder;
import eu.ttbox.geoping.encoder.params.encoding.MultiFieldParamEncoder;
import eu.ttbox.geoping.encoder.params.encoding.StringBase64ParamEncoder;
import eu.ttbox.geoping.encoder.params.encoding.StringParamEncoder;

public class ParamTypeEncoding {

    public static final IParamEncoder STRING = new StringParamEncoder();
    public static final IParamEncoder GPS_PROVIDER = new GpsProviderParamEncoder();
     public static final IParamEncoder STRING_BASE64 = new StringBase64ParamEncoder() ;

    public static final IParamEncoder INT = new IntegerParamEncoder();
    public static final IParamEncoder LONG = new LongParamEncoder();
    public static final IParamEncoder DATE = new DateInSecondeParamEncoder();

    public static final IParamEncoder MULTI = new MultiFieldParamEncoder();

}
