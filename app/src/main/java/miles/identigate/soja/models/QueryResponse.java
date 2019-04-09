package miles.identigate.soja.models;


import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class QueryResponse implements Parcelable {

    @SerializedName("result_code")
    @Expose
    private Integer resultCode;
    @SerializedName("result_text")
    @Expose
    private String resultText;
    @SerializedName("result_content")
    @Expose
    private String resultContent;
    public final static Parcelable.Creator<QueryResponse> CREATOR = new Creator<QueryResponse>() {


        @SuppressWarnings({
                "unchecked"
        })
        public QueryResponse createFromParcel(Parcel in) {
            return new QueryResponse(in);
        }

        public QueryResponse[] newArray(int size) {
            return (new QueryResponse[size]);
        }

    };

    protected QueryResponse(Parcel in) {
        this.resultCode = ((Integer) in.readValue((Integer.class.getClassLoader())));
        this.resultText = ((String) in.readValue((String.class.getClassLoader())));
        this.resultContent = ((String) in.readValue((String.class.getClassLoader())));
    }

    public QueryResponse() {
    }

    public Integer getResultCode() {
        return resultCode;
    }

    public void setResultCode(Integer resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultText() {
        return resultText;
    }

    public void setResultText(String resultText) {
        this.resultText = resultText;
    }

    public String getResultContent() {
        return resultContent;
    }

    public void setResultContent(String resultContent) {
        this.resultContent = resultContent;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(resultCode);
        dest.writeValue(resultText);
        dest.writeValue(resultContent);
    }

    public int describeContents() {
        return 0;
    }

}