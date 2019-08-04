package com.meantime;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class FirebaseUtils {

    public static void trashForOther(String id, List<Friend> friends){
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        String myPhone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        for(int i = 0; i < friends.size(); i++){
            Friend friend = friends.get(i);
            final DatabaseReference taskRef = usersRef.child(friend.getPhoneNumber()).child("taskRequests").child(myPhone).child(id);
            taskRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        taskRef.removeValue();
                    }
                    else{
                        DatabaseReference taskRef1 = usersRef.child(friend.getPhoneNumber()).child("tasks").child(myPhone).child(id);
                        taskRef1.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    taskRef1.removeValue();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}
