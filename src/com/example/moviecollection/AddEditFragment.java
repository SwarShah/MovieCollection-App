// AddEditFragment.java
// Allows user to add a new contact or edit an existing one
package com.example.moviecollection;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class AddEditFragment extends Fragment
{
   // callback method implemented by MainActivity  
   public interface AddEditFragmentListener
   {
      // called after edit completed so contact can be redisplayed
      public void onAddEditCompleted(long rowID);
   }
   
   private AddEditFragmentListener listener; 
   
   private long rowID; // database row ID of the contact
   private Bundle movieInfoBundle; // arguments for editing a contact

   // EditTexts for contact information
   private EditText titleEditText;
   private EditText yearEditText;
   private EditText directorEditText;
   private EditText runtimeEditText;

   // set AddEditFragmentListener when Fragment attached   
   @Override
   public void onAttach(Activity activity)
   {
      super.onAttach(activity);
      listener = (AddEditFragmentListener) activity; 
   }

   // remove AddEditFragmentListener when Fragment detached
   @Override
   public void onDetach()
   {
      super.onDetach();
      listener = null; 
   }
   
   // called when Fragment's view needs to be created
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState)
   {
      super.onCreateView(inflater, container, savedInstanceState);    
      setRetainInstance(true); // save fragment across config changes
      setHasOptionsMenu(true); // fragment has menu items to display
      
      // inflate GUI and get references to EditTexts
      View view = 
         inflater.inflate(R.layout.fragment_add_edit, container, false);
      titleEditText = (EditText) view.findViewById(R.id.titleEditText);
      yearEditText = (EditText) view.findViewById(R.id.yearEditText);
      directorEditText = (EditText) view.findViewById(R.id.directorEditText);
      runtimeEditText = (EditText) view.findViewById(R.id.runtimeEditText);
     

      movieInfoBundle = getArguments(); // null if creating new contact

      if (movieInfoBundle != null)
      {
         rowID = movieInfoBundle.getLong(MainActivity.ROW_ID);
         titleEditText.setText(movieInfoBundle.getString("title"));  
         yearEditText.setText(movieInfoBundle.getString("year"));  
         directorEditText.setText(movieInfoBundle.getString("director"));  
         runtimeEditText.setText(movieInfoBundle.getString("runtime"));   
      } 
      
      // set Save Contact Button's event listener 
      Button saveMovieButton = 
         (Button) view.findViewById(R.id.saveMovie);
      saveMovieButton.setOnClickListener(saveMovieButtonClicked);
      return view;
   }

   // responds to event generated when user saves a contact
   OnClickListener saveMovieButtonClicked = new OnClickListener() 
   {
      @Override
      public void onClick(View v) 
      {
         if (titleEditText.getText().toString().trim().length() != 0)
         {
            // AsyncTask to save contact, then notify listener 
            AsyncTask<Object, Object, Object> saveMovieTask = 
               new AsyncTask<Object, Object, Object>() 
               {
                  @Override
                  protected Object doInBackground(Object... params) 
                  {
                     saveMovie(); // save contact to the database
                     return null;
                  } 
      
                  @Override
                  protected void onPostExecute(Object result) 
                  {
                     // hide soft keyboard
                     InputMethodManager imm = (InputMethodManager) 
                        getActivity().getSystemService(
                           Context.INPUT_METHOD_SERVICE);
                     imm.hideSoftInputFromWindow(
                        getView().getWindowToken(), 0);

                     listener.onAddEditCompleted(rowID);
                  } 
               }; // end AsyncTask
               
            // save the contact to the database using a separate thread
            saveMovieTask.execute((Object[]) null); 
         } 
         else // required contact name is blank, so display error dialog
         {
            DialogFragment errorSaving = 
               new DialogFragment()
               {
                  @Override
                  public Dialog onCreateDialog(Bundle savedInstanceState)
                  {
                     AlertDialog.Builder builder = 
                        new AlertDialog.Builder(getActivity());
                     builder.setMessage(R.string.error_message);
                     builder.setPositiveButton(R.string.ok, null);                     
                     return builder.create();
                  }               
               };
            
            errorSaving.show(getFragmentManager(), "error saving movie");
         } 
      } // end method onClick
   }; // end OnClickListener saveContactButtonClicked

   // saves contact information to the database
   private void saveMovie() 
   {
      // get DatabaseConnector to interact with the SQLite database
      DatabaseConnector databaseConnector = 
         new DatabaseConnector(getActivity());

      if (movieInfoBundle == null)
      {
         // insert the contact information into the database
    	  	rowID = databaseConnector.insertMovie(
            titleEditText.getText().toString(),
            yearEditText.getText().toString(), 
            directorEditText.getText().toString(), 
            runtimeEditText.getText().toString());
      } 
      else
      {
         databaseConnector.updateMovie(rowID,
            titleEditText.getText().toString(),
            yearEditText.getText().toString(), 
            directorEditText.getText().toString(), 
            runtimeEditText.getText().toString());            
      }
   } // end method saveContact
} // end class AddEditFragment

