package com.dragon4.owo.ar_trace.ARCore.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dragon4.owo.ar_trace.Configure.ClientInstance;
import com.dragon4.owo.ar_trace.Model.Trace;
import com.dragon4.owo.ar_trace.Model.User;
import com.dragon4.owo.ar_trace.Network.ClientSelector;
import com.dragon4.owo.ar_trace.R;
import com.tsengvn.typekit.TypekitContextWrapper;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class WriteReviewActivity extends Activity implements View.OnClickListener {
    //middle image between picture chooser and video chooser.
    private ImageView middleImg;

    //take picture request number
    private final int REQUEST_IMAGE = 1;

    //save file destination after taking picture.
    private File destination = null;

    private Context context;

    //choosed bitmap
    private Bitmap currentBitmap = null;

    private Double currentLat;
    private Double currentLon;
    private String placeName;

    private User currentUser;

    TextView axisTitle;
    TextView axisNum;
    TextView locationTitle;
    TextView locationName;

    private ClientSelector clientSelector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_ar_mixview_write_review);
        loadActivity();

        clientSelector = ClientInstance.getInstanceClient(); // 추후에 파이썬 서버 버젼도 가능케 할 예정

       // clientSelector = new PythonClient();
        currentUser = User.getMyInstance();
    }

    public void loadActivity() {

        context = getApplicationContext();
        middleImg = (ImageView) findViewById(R.id.ar_mixview_write_review_middle_img);

        //register onclick listener
        findViewById(R.id.ar_mixview_write_review_back).setOnClickListener(this);
        findViewById(R.id.ar_mixview_write_review_axis).setOnClickListener(this);
        findViewById(R.id.ar_mixview_write_review_location).setOnClickListener(this);
        findViewById(R.id.ar_mixview_write_review_add).setOnClickListener(this);
        findViewById(R.id.ar_mixview_write_review_register).setOnClickListener(this);
        axisTitle = (TextView) findViewById(R.id.ar_mixview_write_review_axis_title);
        axisNum = (TextView) findViewById(R.id.ar_mixview_write_review_axis_num);
        locationTitle = (TextView) findViewById(R.id.ar_mixview_write_review_location_title);
        locationName = (TextView) findViewById(R.id.ar_mixview_write_review_location_name);

        currentLat = getIntent().getDoubleExtra("lat", 0.0);
        currentLon = getIntent().getDoubleExtra("lon", 0.0);

        // 경도 위도
        axisNum.setText(String.valueOf(String.valueOf(currentLat) + " N " + String.valueOf(currentLon) + " E "));

        // 건물의 위치를 기반으로 리뷰를 씀. 여기서 주소좌표계는 31XXXXXX.XX구조 좌표게가 서로다름.
        locationName.setText(getIntent().getStringExtra("buildingID"));
        placeName = getIntent().getStringExtra("buildingID");

        // 내 위치를 기반으로 주소를 가져오고 리뷰를 씀. 여기서 주소좌표계는 31.XXXXXX구조 좌표계가 서로 다름.
        /*
        String requestReverseGeoAPI = DataSource.createNaverReverseGeoAPIRequcetURL(currentLat, currentLon);

        try {
            // 주소 얻기
            String reverseGeoString = new NaverHttpHandler().execute(requestReverseGeoAPI).get();
            String placeAddress;
            placeAddress = parsingReverseGeoJson(reverseGeoString);
            placeName = placeAddress;
            placeAddress = URLEncoder.encode(placeAddress,"UTF-8");
            Log.i("placeAddress RealName", placeAddress);

            // 주소로 장소 이름 얻기
            String requestRealPlaceName = DataSource.createNaverSearchRequestURL(placeAddress);
            String realPlaceNameString = new NaverHttpHandler().execute(requestRealPlaceName).get();
            Log.i("Place RealName JSON", realPlaceNameString);
            String place = parsingPlaceNameJson(realPlaceNameString);
            Log.i("parsing RealName JSON", place);

            if(place.length() > 0 ) { // 장소이름이 검색될때.
                placeName = place;
                locationName.setText(placeName);
            }

            Log.i("parsing Address", locationName.getText().toString());

        } catch (InterruptedException | JSONException | ExecutionException e) {
            locationName.setText(placeName);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        */
    }

    /*
    public String parsingPlaceNameJson(String realPlaceNameString) throws JSONException {
        String place = "";
        JSONObject reverseObject = new JSONObject(realPlaceNameString);
        JSONArray dataList = reverseObject.getJSONObject("result").getJSONArray("items");
        place = dataList.getJSONObject(0).getString("title");
        return place;
    }

    public String parsingReverseGeoJson(String reverseGeoString) throws JSONException {
        String locationName = "";
        JSONObject reverseObject = new JSONObject(reverseGeoString);
        JSONArray dataList = reverseObject.getJSONObject("result").getJSONArray("items");
        for (int i = 0; i < dataList.length(); i++) {
            locationName = dataList.getJSONObject(i).getString("address");
        }
        return locationName;
    }
 */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ar_mixview_write_review_back:
                finish();
                break;

            case R.id.ar_mixview_write_review_axis:
                chooseAxis();
                break;

            case R.id.ar_mixview_write_review_location:
                chooseLocation();
                break;

            case R.id.ar_mixview_write_review_add:
                choosePictureCase();
                break;

            case R.id.ar_mixview_write_review_register:
                makeTraceInstanceToServer();
                break;
        }
    }

    private void chooseAxis() {
        middleImg.setImageResource(R.drawable.icon_rhombus_left_chosen);
        axisTitle.setTextColor(Color.parseColor("#A6000000"));
        axisNum.setTextColor(Color.parseColor("#A6000000"));
        locationTitle.setTextColor(Color.parseColor("#5B000000"));
        locationName.setTextColor(Color.parseColor("#42000000"));
    }

    private void chooseLocation() {
        middleImg.setImageResource(R.drawable.icon_rhombus_right_chosen);
        axisTitle.setTextColor(Color.parseColor("#5B000000"));
        axisNum.setTextColor(Color.parseColor("#42000000"));
        locationTitle.setTextColor(Color.parseColor("#A6000000"));
        locationName.setTextColor(Color.parseColor("#A6000000"));

    }

    private void choosePictureCase() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/ARTrace/");

        if (!file.exists())
            file.mkdir();

        destination = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/ARTrace/" + System.currentTimeMillis() + ".jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(destination));
        startActivityForResult(intent, REQUEST_IMAGE);
    }


    public int exifOrientationToDegrees(int exifOrientation)
    {
        if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_90)
        {
            return 90;
        }
        else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_180)
        {
            return 180;
        }
        else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_270)
        {
            return 270;
        }
        return 0;
    }

    public Bitmap rotate(Bitmap bitmap, int degrees)
    {
        if(degrees != 0 && bitmap != null)
        {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2,
                    (float) bitmap.getHeight() / 2);

            try
            {
                Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), m, true);
                if(bitmap != converted)
                {
                    bitmap.recycle();
                    bitmap = converted;
                }
            }
            catch(OutOfMemoryError ex)
            {
                // 메모리가 부족하여 회전을 시키지 못할 경우 그냥 원본을 반환합니다.
            }
        }
        return bitmap;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                try {
                    // if python server on
                    //uploadImageToPythonServer(Uri.fromFile(destination));
                    currentBitmap = BitmapFactory.decodeFile(Uri.fromFile(destination).getPath());

                    ExifInterface exifInterface = new ExifInterface(Uri.fromFile(destination).getPath());
                    int imageOrientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    int exifInterfaceDegree = exifOrientationToDegrees(imageOrientation);
                    currentBitmap = rotate(currentBitmap,exifInterfaceDegree);

                    ImageView currentImageView = new ImageView(this);
                    View picture = findViewById(R.id.ar_mixview_write_review_add);
                    currentImageView.setImageBitmap(currentBitmap);

                    double scale;
                    if(currentBitmap.getWidth() > picture.getMeasuredWidth())
                        scale = (double)picture.getMeasuredWidth() / currentBitmap.getWidth();
                    else
                        scale = 1;
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int)(currentBitmap.getWidth() * scale), (int)(currentBitmap.getHeight() * scale));
                    currentImageView.setLayoutParams(params);

                    //delete current pictureview and add new pictureview
                    ViewGroup parent = (ViewGroup) picture.getParent();
                    int index = parent.indexOfChild(picture);
                    parent.removeView(picture);
                    parent.addView(currentImageView, index);

                    //set id and register onclicklistener
                    currentImageView.setId(R.id.ar_mixview_write_review_add);
                    currentImageView.setOnClickListener(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "사진을 취소하셨습니다.", Toast.LENGTH_SHORT).show();
                destination.delete();
            }
        }
    }

    private void makeTraceInstanceToServer() {
        Trace trace = new Trace();
        //set location id of trace
        //String hashKey = MixUtils.makeHashStringMD5(currentLon, currentLon); // 길거리일때 .
        trace.setLocationID(placeName); // 장소의 이름으로 키값을 설정한다

        //add content to trace
        EditText content = (EditText) findViewById(R.id.ar_mixview_write_review_content);
        if (content != null)
            trace.setContent(content.getText().toString());
        else
            trace.setContent("");
        // 이미지 url , 썸네일 url 추가

        if (currentBitmap != null)
            clientSelector.uploadImageToServer(trace,destination);

        trace.setTraceID(placeName);
        trace.setLat(currentLat); // 경도
        trace.setLon(currentLon); // 위도
        trace.setPlaceName(placeName); // 장소이름
        trace.setWriteDate(new Date().getTime()); // 업로드    날짜

        trace.setUserName(currentUser.getUserName()); // 유조이름
        trace.setUserImageUrl(currentUser.getUserImageURL()); // 유저 이미지 유알엘
        trace.setUserToken(currentUser.getUserToken());
        trace.setuserID(currentUser.getuserID());

        clientSelector.uploadTraceToServer(trace);
        Toast.makeText(getApplicationContext(),"글이 등록되었습니다.", Toast.LENGTH_SHORT). show();
        finish();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        setResult(RESULT_OK);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }
}