package com.example.futuregram3;

        import androidx.annotation.NonNull;
        import androidx.annotation.Nullable;
        import androidx.appcompat.app.AppCompatActivity;

        import android.content.Context;
        import android.content.Intent;
        import android.content.pm.PackageInfo;
        import android.content.pm.PackageManager;
        import android.content.pm.Signature;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ImageView;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.google.android.gms.tasks.OnCompleteListener;
        import com.google.android.gms.tasks.Task;
        import com.google.firebase.database.DataSnapshot;
        import com.google.firebase.database.DatabaseError;
        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;
        import com.google.firebase.database.ValueEventListener;
        import com.google.firebase.iid.FirebaseInstanceId;
        import com.google.firebase.iid.InstanceIdResult;
        import com.kakao.auth.ISessionCallback;
        import com.kakao.auth.Session;
        import com.kakao.network.ErrorResult;
        import com.kakao.usermgmt.LoginButton;
        import com.kakao.usermgmt.UserManagement;
        import com.kakao.usermgmt.callback.MeV2ResponseCallback;
        import com.kakao.usermgmt.response.MeV2Response;
        import com.kakao.util.exception.KakaoException;
        import com.kakao.util.helper.log.Logger;

        import java.security.MessageDigest;
        import java.security.NoSuchAlgorithmException;
        import java.util.ArrayList;
        import java.util.List;

        import static com.kakao.util.helper.Utility.getPackageInfo;

public class LoginActivity extends AppCompatActivity {



    static String TAG="yeon1216  ";
    static LoginActivity loginActivity;

    EditText idInputET; // ????????? ?????? ???
    EditText passInputET; // ???????????? ?????? ???
    LoginButton com_kakao_login; // ????????? ????????? ??????

    MyAppData myAppData; // ??? ?????? ?????????
    MyAppService myAppService; // ??? ?????? ?????????

    private SessionCallback sessionCallback;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginActivity=LoginActivity.this;

        // fcm ?????? ?????? ???
//        FirebaseInstanceId.getInstance().getInstanceId()
//                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
//                        if (!task.isSuccessful()) {
//                            Log.w(TAG, "getInstanceId failed", task.getException());
//                            return;
//                        }
//
//                        // Get new Instance ID token
//                        String token = task.getResult().getToken();
//                        Log.w(TAG, "token : " + token);
//                        // Log and toast
////                        String msg = getString(R.string.msg_token_fmt, token);
////                        Log.d(TAG, msg);
////                        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
//                    }
//                });

        myAppService = new MyAppService(); // ??? ?????? ????????? ?????? ??????


//        myAppService.initData(this);




        myAppData = myAppService.readAllData(this); // ??? ????????? ????????? ?????? ????????? ??? ?????? ???????????? ????????????

        if(myAppData.memberCount==-1){
            myAppData = myAppService.initData(this);
        }



//        int loginMemberNo = myAppService.autoLoginCheck(this);
        // ?????? ?????????
        if(myAppData.loginMemberNo!=-1){
            // ???????????? ????????????
            Intent intent = new Intent(getApplicationContext(),LoadingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);

            // ????????? ?????? ???????????? ??? ?????????
            finish();
        }






        idInputET = findViewById(R.id.idInputET); // ????????? ?????? ???
        passInputET = findViewById(R.id.passInputET);  // ???????????? ?????? ???

        Button loginBtn = findViewById(R.id.loginBtn); // ????????? ??????
        // ????????? ?????? ????????? ?????????
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inputPhoneNum = idInputET.getText().toString().trim(); // ????????? ????????????
                String password = passInputET.getText().toString().trim(); // ????????? ????????????
                int loginMemberNo = myAppService.login(myAppData,inputPhoneNum,password); // ?????????????????? ??????
                if(loginMemberNo==-1){
                    Toast.makeText(LoginActivity.this,"????????? ??????",Toast.LENGTH_SHORT).show();
                }else{
                    myAppData.loginMemberNo= loginMemberNo;
                    Intent intent = new Intent(getApplicationContext(),LoadingActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                    finish(); // ????????? activity finish;
                }
            }
        });


        TextView joinTV = findViewById(R.id.joinTV); // ???????????? ????????????
        // ???????????? ???????????? ????????? ?????????
        joinTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),JoinActivity.class);
//                intent.putExtra("myAppData",myAppData);
                startActivity(intent);
            }
        });

        TextView loginByPhoneTV = findViewById(R.id.loginByPhoneTV); // ??????????????? ??????????????? ????????????
        // ??????????????? ??????????????? ???????????? ????????? ?????????
        loginByPhoneTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),LoginByPhoneActivity.class);
//                intent.putExtra("myAppData",myAppData);
                startActivity(intent);
            }
        });

        ImageView loginByKakaoIV = findViewById(R.id.loginByKakaoIV); // ???????????? ????????? ????????????
        // ???????????? ????????? ???????????? ????????? ?????????
        loginByKakaoIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                com_kakao_login.performClick();
//                Toast.makeText(getApplicationContext(),"???????????? ????????? (?????? ??????)",Toast.LENGTH_SHORT).show();

            }
        });

        com_kakao_login = findViewById(R.id.com_kakao_login);

        // ????????? ???????????? ??????
        sessionCallback = new LoginActivity.SessionCallback();
        Session.getCurrentSession().addCallback(sessionCallback);
        Session.getCurrentSession().checkAndImplicitOpen();

    } // onCreate ?????????

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.w("kakao","onActivityResult() ??????");
        if(Session.getCurrentSession().handleActivityResult(requestCode,resultCode,data)){
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    } // onActivityResult() ?????????

    @Override
    protected void onStart() {
        super.onStart();
        Log.w(TAG+this.getClass().getSimpleName(),"onStart() ??????");

//        DatabaseReference memberCountDatabaseReference = FirebaseDatabase.getInstance().getReference("myAppData").child("memberCount");
//        memberCountDatabaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                memberCount = Integer.parseInt(dataSnapshot.getValue().toString());
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w(TAG+this.getClass().getSimpleName(),"onResume() ??????");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.w(TAG+this.getClass().getSimpleName(),"onPause() ??????");
        myAppService.writeAllData(myAppData,this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.w(TAG+this.getClass().getSimpleName(),"onDestroy() ??????");
        Session.getCurrentSession().removeCallback(sessionCallback);
    }


    // SessionCallback inner ????????? : ?????? ????????? ?????? ????????? ?????? ??????. ????????? ??????????????? ???, ????????? ????????? ??? ?????? ????????? ????????? ??????.
    private class SessionCallback implements ISessionCallback {

        @Override
        public void onSessionOpened() {
            Log.w("kakao","onSessionOpened() ??????");
            Log.w("kakao","onSessionOpened() ????????? ????????? ??????");
//            redirectSignupActivity();
            requestMe();
        } // onSessionOpened() ????????? : accesstoken??? ??????????????? ?????? ?????? valid access token??? ????????? ?????? ??????. ??????????????? ????????? ?????? ?????? activity??? ????????????.

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            Log.w("kakao","onSessionOpenFailed() ??????");
            Log.w("kakao","onSessionOpenFailed()   "+exception.toString());
            if(exception!=null){
                Logger.e(exception);
            }
        } // onSessionOpenFailed() ????????? : memory??? cache??? session ????????? ?????? ?????? ??????. ??????????????? ????????? ????????? ????????? ???????????? ????????? ????????? ?????? access token ????????? ????????????.

    } // SessionCallback inner ?????????

    private void requestMe(){
        ArrayList<String> keys = new ArrayList<>();
        keys.add("properties.nickname");
        keys.add("properties.profile_image");
        keys.add("kakao_account.email");

        UserManagement.getInstance().me(keys, new MeV2ResponseCallback() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                super.onFailure(errorResult);
                Log.w("kakao", "requestMe onFailure message : " + errorResult.getErrorMessage());
            }

            @Override
            public void onFailureForUiThread(ErrorResult errorResult) {
                super.onFailureForUiThread(errorResult);
                Log.w("kakao", "requestMe onFailureForUiThread message : " + errorResult.getErrorMessage());
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                Log.w("kakao", "requestMe onSessionClosed message : " + errorResult.getErrorMessage());
            }

            @Override
            public void onSuccess(MeV2Response result) {
                Log.w("kakao", "requestMe onSuccess message : " + result.getId() + " " + result.getNickname());
                boolean isMember = false;
                for(Member member : myAppData.members){
                    if(member.phone.equals(String.valueOf(result.getId()))){
                        isMember=true;
                    }
                }
                int loginMemberNo=-1;
                if(isMember){
                    Log.w("kakao", "????????? ????????? ???????????? ????????? ??????");
                    for(Member member : myAppData.members){
                        if(member.phone.equals(String.valueOf(result.getId()))){
                            loginMemberNo = member.memberNo;
                        }
                    }
                    myAppData.loginMemberNo= loginMemberNo;
                    Intent intent = new Intent(getApplicationContext(),LoadingActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                    finish(); // ????????? activity finish;
                }else{
                    Log.w("kakao", "????????? ????????? ???????????? ????????? ?????? ??????");
//                    Member joinMember = new Member(memberCount,String.valueOf(result.getId()),"???kaO123!@#","00000000000");

                    myAppService.join(myAppData,String.valueOf(result.getId()),"???kaO123!@#","00000000000",myAppData.memberCount,getApplicationContext());
//                    myAppData.memberCount++;
//                    myAppData.members.add(joinMember);
//                    myAppService.writeAllData(myAppData,LoginActivity.this);
//                    loginMemberNo = joinMember.memberNo;
                    myAppData.loginMemberNo= myAppData.memberCount-1;
                    Intent intent = new Intent(getApplicationContext(),NickNameInputActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    finish(); // ????????? activity finish;
                }
            } // onSuccess() ?????????

        });

    } // requestMe() ?????????

    public static String getKeyHash(final Context context) {
        PackageInfo packageInfo = getPackageInfo(context, PackageManager.GET_SIGNATURES);
        if (packageInfo == null)
            return null;

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                return android.util.Base64.encodeToString(md.digest(), android.util.Base64.NO_WRAP);
            } catch (NoSuchAlgorithmException e) {
                Log.w("kakao", "Unable to get MessageDigest. signature=" + signature, e);
            }
        }
        return null;
    }

} // LoginActivity ?????????

