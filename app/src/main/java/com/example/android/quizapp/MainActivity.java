package com.example.android.quizapp;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Toast;

import static android.content.DialogInterface.BUTTON_POSITIVE;
import static android.content.DialogInterface.OnClickListener;

enum AnswerState {
    ANSWER_CORRECT,
    ANSWER_WRONG,
    ANSWER_EMPTY
}

interface AnswerChecker {
    String STORAGE_KEY_NAME = "Question_";

    AnswerState VerifyAnswer();
    AnswerState GetCachedAnswerState();

    void HighlightAsCorrect(AppCompatActivity parent);
    void HighlightAsWrong(AppCompatActivity parent);

    //Saves the highlight colors at key name "Question_<slot>".
    //For example, question 5 will have this array saved at key "Question_4".
    void SaveVisualState(Bundle savedInstanceState, int slotId);
    //Restores the highlight colors at key name "Question_<slot>", for question number "slotId + 1".
    void RestoreVisualState(Bundle savedInstanceState, int slotId);
}

class CheckboxAnswer implements AnswerChecker {
    //For checkbox/radio type answers.
    private final CheckBox correctOptionCtrls[];
    private final CheckBox wrongOptionCtrls[];
    private final int originalTextColor;
    //Saved visual state, used for instance save and restore.
    private int optionsColors[];
    //Cached evaluation state.
    private AnswerState cachedAnswerState;


    //Constructor for questions with multiple valid answers.
    CheckboxAnswer(AppCompatActivity parent,
                   int correctAnswersIds[], int wrongAnswersIds[]) {
        correctOptionCtrls = new CheckBox[correctAnswersIds.length];
        for (int i = 0; i < correctAnswersIds.length; ++i)
            correctOptionCtrls[i] = parent.findViewById(correctAnswersIds[i]);

        wrongOptionCtrls = new CheckBox[wrongAnswersIds.length];
        for (int i = 0; i < wrongAnswersIds.length; ++i)
            wrongOptionCtrls[i] = parent.findViewById(wrongAnswersIds[i]);

        originalTextColor = correctOptionCtrls[0].getCurrentTextColor();

        optionsColors = new int[correctAnswersIds.length + wrongAnswersIds.length];
    }

    public AnswerState VerifyAnswer() {
        //Check if there is any checked and wrong answer. In this case, the overall answer is wrong.
        for (CheckBox crtCheckbox : wrongOptionCtrls) {
            if (crtCheckbox.isChecked())
                return cachedAnswerState = AnswerState.ANSWER_WRONG;
        }

        //At this stage, the answer can be correct or empty.
        //Count the number of checked and correct answers.
        int numCorrectAnswers = 0;
        for (CheckBox crtCheckbox : correctOptionCtrls) {
            if (crtCheckbox.isChecked())
                ++numCorrectAnswers;
        }

        //If there are no correct answers checked, then we have an empty answer.
        //At this stage, all the wrong answers are also unchecked.
        if (numCorrectAnswers == 0)
            return cachedAnswerState = AnswerState.ANSWER_EMPTY;

        //If only part of the correct answers are checked, then we have a wrong (incomplete)
        //answer.
        if (numCorrectAnswers != correctOptionCtrls.length)
            return cachedAnswerState = AnswerState.ANSWER_WRONG;

        //At this point, all the correct answers are checked and all the wrong answers
        //are unchecked.
        return cachedAnswerState = AnswerState.ANSWER_CORRECT;
    }

    public void HighlightAsCorrect(AppCompatActivity parent) {
        Resources res = parent.getResources();
        int correctOptionColor = res.getColor(R.color.correctAnswer);
        //int wrongOptionColor = res.getColor(R.color.wrongAnswer);

        for (CheckBox checkBox : correctOptionCtrls) {
            checkBox.setTextColor(correctOptionColor);
        }
        for (CheckBox checkBox : wrongOptionCtrls) {
            checkBox.setTextColor(originalTextColor);
        }

    }

    public void HighlightAsWrong(AppCompatActivity parent) {
        Resources res = parent.getResources();
        int correctOptionColor = res.getColor(R.color.correctAnswer);
        int wrongOptionColor = res.getColor(R.color.wrongAnswer);

        for (CheckBox checkBox : correctOptionCtrls) {
            checkBox.setTextColor(correctOptionColor);
        }
        //Highlight the wrong answers only if the user checked them.
        for (CheckBox checkBox : wrongOptionCtrls) {
            if (checkBox.isChecked())
                checkBox.setTextColor(wrongOptionColor);
            else
                checkBox.setTextColor(originalTextColor);
        }
    }

    public void SaveVisualState(Bundle savedInstanceState, int slotId) {
        //Save the highlight color in an internal vector.
        for (int i=0; i<correctOptionCtrls.length; ++i) {
            optionsColors[i] = correctOptionCtrls[i].getCurrentTextColor();
        }
        for (int i=0; i<wrongOptionCtrls.length; ++i) {
            optionsColors[correctOptionCtrls.length + i] = wrongOptionCtrls[i].getCurrentTextColor();
        }

        //Output the color value into an integer array, at a key name depending on the slot index.
        String keyName = STORAGE_KEY_NAME + Integer.toString(slotId);
        savedInstanceState.putIntArray(keyName, optionsColors);
    }
    public void RestoreVisualState(Bundle savedInstanceState, int slotId) {
        String keyName = STORAGE_KEY_NAME + Integer.toString(slotId);
        int newOptionsColors[] = savedInstanceState.getIntArray(keyName);
        if(newOptionsColors != null)
            optionsColors = newOptionsColors;

        for (int i=0; i<correctOptionCtrls.length; ++i) {
            correctOptionCtrls[i].setTextColor(optionsColors[i]);
        }
        for (int i=0; i<wrongOptionCtrls.length; ++i) {
            wrongOptionCtrls[i].setTextColor(optionsColors[correctOptionCtrls.length + i]);
        }
    }

    public AnswerState GetCachedAnswerState() {
        return cachedAnswerState;
    }
}

class RadioAnswer implements AnswerChecker {
    //For checkbox/radio type answers.
    private final RadioButton correctOptionCtrl;
    private final RadioButton wrongOptionCtrls[];
    private final int originalTextColor;
    //Saved visual state, used for instance save and restore.
    private int optionsColors[];
    //Cached answer state.
    private AnswerState cachedAnswerState;

    //Constructor for questions with only one valid answer (radio button).
    //The wrong answer controls are not needed for verification, since radio button groups
    //restrict the selection to a single item. They are only needed for highlighting
    //the wrong answer, if checked.
    RadioAnswer(AppCompatActivity parent,
                int correctAnswerId, int wrongAnswersIds[]) {
        correctOptionCtrl = parent.findViewById(correctAnswerId);

        wrongOptionCtrls = new RadioButton[wrongAnswersIds.length];
        for (int i = 0; i < wrongAnswersIds.length; ++i)
            wrongOptionCtrls[i] = parent.findViewById(wrongAnswersIds[i]);

        originalTextColor = wrongOptionCtrls[0].getCurrentTextColor();

        optionsColors = new int [1 + wrongOptionCtrls.length];
    }

    public AnswerState VerifyAnswer() {
        //If the correct answer radio is checked, so the result is correct.
        if (correctOptionCtrl.isChecked()) {
            return cachedAnswerState = AnswerState.ANSWER_CORRECT;
        }
        //If any of the wrong answer radios is checked, then the answer is wrong overall.
        for (RadioButton radioBtn : wrongOptionCtrls) {
            if (radioBtn.isChecked()) {
                return cachedAnswerState = AnswerState.ANSWER_WRONG;
            }
        }

        //We reached so far, meaning that no radio options have been checked, so the answer is empty.
        return cachedAnswerState = AnswerState.ANSWER_EMPTY;
    }

    public void HighlightAsCorrect(AppCompatActivity parent) {
        Resources res = parent.getResources();
        int correctOptionColor = res.getColor(R.color.correctAnswer);

        //Highlight the correct answer in green.
        correctOptionCtrl.setTextColor(correctOptionColor);
        for (RadioButton radioButton : wrongOptionCtrls) {
            radioButton.setTextColor(originalTextColor);
        }
    }

    public void HighlightAsWrong(AppCompatActivity parent) {
        Resources res = parent.getResources();
        int correctOptionColor = res.getColor(R.color.correctAnswer);
        int wrongOptionColor = res.getColor(R.color.wrongAnswer);

        //Emphasize the wrong radio options which have been checked.
        for (RadioButton radioButton : wrongOptionCtrls) {
            if (radioButton.isChecked()) {
                radioButton.setTextColor(wrongOptionColor);

            } else
                radioButton.setTextColor(originalTextColor);
        }
        //Highlight the correct answer in green.
        correctOptionCtrl.setTextColor(correctOptionColor);
    }


    public void SaveVisualState(Bundle savedInstanceState, int slotId) {
        optionsColors[0] = correctOptionCtrl.getCurrentTextColor();
        for (int i=0; i<wrongOptionCtrls.length; ++i) {
            optionsColors[1 + i] = wrongOptionCtrls[i].getCurrentTextColor();
        }

        String keyName = STORAGE_KEY_NAME + Integer.toString(slotId);
        savedInstanceState.putIntArray(keyName, optionsColors);
    }
    public void RestoreVisualState(Bundle savedInstanceState, int slotId) {
        String keyName = STORAGE_KEY_NAME + Integer.toString(slotId);
        int newOptionsColors[] = savedInstanceState.getIntArray(keyName);
        if(newOptionsColors != null)
            optionsColors = newOptionsColors;

        correctOptionCtrl.setTextColor(optionsColors[0]);
        for (int i=0; i<wrongOptionCtrls.length; ++i) {
            wrongOptionCtrls[i].setTextColor(optionsColors[1 + i]);
        }
    }

    public AnswerState GetCachedAnswerState() {
        return cachedAnswerState;
    }
}

class EditableAnswer implements AnswerChecker {
    //For input text type answers.
    private final EditText textAnswerCtrl;
    private final String correctAnswerValue;
    //Cached evaluation state.
    private AnswerState cachedAnswerState;


    //Constructor for questions with editable text answers.
    EditableAnswer(AppCompatActivity parent,
                   int textAnswerId, String correctAnswerValue) {
        textAnswerCtrl = parent.findViewById(textAnswerId);
        this.correctAnswerValue = correctAnswerValue;
    }

    public AnswerState VerifyAnswer() {
        //Text type result.
        //Trim the leading and trailing whitespaces to avoid typing errors.
        String existingResult = textAnswerCtrl.getText().toString()
                .trim();
        if (existingResult.isEmpty())
            return cachedAnswerState = AnswerState.ANSWER_EMPTY;

        //Compare the result in case insensitive mode, it's more tolerant to typos.
        if (!existingResult.equalsIgnoreCase(correctAnswerValue))
            return cachedAnswerState = AnswerState.ANSWER_WRONG;

        return cachedAnswerState = AnswerState.ANSWER_CORRECT;
    }

    public void HighlightAsCorrect(AppCompatActivity parent) {
        Resources res = parent.getResources();
        int correctAnswerColor = res.getColor(R.color.correctAnswer);

        //Just highlight the correct answer in green.
        textAnswerCtrl.setTextColor(correctAnswerColor);
    }

    public void HighlightAsWrong(AppCompatActivity parent) {
        Resources res = parent.getResources();
        int wrongAnswerColor = res.getColor(R.color.wrongAnswer);

        //Highlight the assumed wrong result in red.
        textAnswerCtrl.setTextColor(wrongAnswerColor);
    }

    public void SaveVisualState(Bundle savedInstanceState, int slotId) {
        int textColor = textAnswerCtrl.getCurrentTextColor();

        String keyName = STORAGE_KEY_NAME + Integer.toString(slotId);
        savedInstanceState.putInt(keyName, textColor);
    }
    public void RestoreVisualState(Bundle savedInstanceState, int slotId) {
        String keyName = STORAGE_KEY_NAME + Integer.toString(slotId);
        int newTextColor = savedInstanceState.getInt(keyName);

        textAnswerCtrl.setTextColor(newTextColor);
    }

    public AnswerState GetCachedAnswerState() {
        return cachedAnswerState;
    }
}

public class MainActivity extends AppCompatActivity {
    private final static int NUM_QUESTIONS = 10;
    private final AnswerChecker answerCheckers[] = new AnswerChecker[NUM_QUESTIONS];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Create an answer checker object for each question, depending on the type of the
        //question.
        //A better solution would be to get the type of the question from the corresponding
        //XML resource, but that would be too complicated.
        answerCheckers[0] = new RadioAnswer(this,
                R.id.radio_q1_a2,
                new int[]{R.id.radio_q1_a1, R.id.radio_q1_a3});
        answerCheckers[1] = new CheckboxAnswer(this,
                new int[]{R.id.checkbox_q2_a2, R.id.checkbox_q2_a3},
                new int[]{R.id.checkbox_q2_a1});
        answerCheckers[2] = new EditableAnswer(this,
                R.id.text_answer_q3, getString(R.string.q3answer));
        answerCheckers[3] = new RadioAnswer(this,
                R.id.radio_q4_a1,
                new int[]{R.id.radio_q4_a2, R.id.radio_q4_a3});
        answerCheckers[4] = new RadioAnswer(this,
                R.id.radio_q5_a3,
                new int[]{R.id.radio_q5_a1, R.id.radio_q5_a2, R.id.radio_q5_a4});
        answerCheckers[5] = new RadioAnswer(this,
                R.id.radio_q6_a1,
                new int[]{R.id.radio_q6_a2, R.id.radio_q6_a3, R.id.radio_q6_a4});
        answerCheckers[6] = new CheckboxAnswer(this,
                new int[]{R.id.checkbox_q7_a1, R.id.checkbox_q7_a3},
                new int[]{R.id.checkbox_q7_a2});
        answerCheckers[7] = new RadioAnswer(this,
                R.id.radio_q8_a1,
                new int[]{R.id.radio_q8_a2});
        answerCheckers[8] = new RadioAnswer(this,
                R.id.radio_q9_a2,
                new int[]{R.id.radio_q9_a1, R.id.radio_q9_a3, R.id.radio_q9_a4});
        answerCheckers[9] = new RadioAnswer(this,
                R.id.radio_q10_a3,
                new int[]{R.id.radio_q10_a1, R.id.radio_q10_a2, R.id.radio_q10_a4});

        if (savedInstanceState != null) {
            for (int i = 0; i < answerCheckers.length; ++i) {
                final AnswerChecker checker = answerCheckers[i];
                checker.RestoreVisualState(savedInstanceState, i);
            }
        }
    }

    /**
     * Save the user's current game state (scores, games, sets, points)
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        // Save the user's current quiz state
        for (int i = 0; i < answerCheckers.length; ++i) {
            final AnswerChecker checker = answerCheckers[i];
            checker.SaveVisualState(savedInstanceState, i);
        }
    }

    public void onClickSubmit(View v) {
        int wrongAnswerCount = 0;
        int emptyAnswerCount = 0;
        EditText nameField = findViewById(R.id.player_name);
        String name = nameField.getText().toString();
        //I found some code using StringBuilder and I thought it's easier to use, since it can
        //append several types of variables.
        //This message is displayed when the user leaves more than one question unanswered.
        StringBuilder emptyAnswerMsg = new StringBuilder(getString(R.string.empty_answers_msg, name));
        //This message is displayed when the user leaves one question unanswered.
        StringBuilder emptyAnswerMsgOne = new StringBuilder(getString(R.string.empty_answers_msg1, name));
        //This string holds the list of indices for the unanswered questions.
        StringBuilder questionList = new StringBuilder();

        for (int i = 0; i < answerCheckers.length; ++i) {
            final AnswerChecker checker = answerCheckers[i];
            AnswerState answerState = checker.VerifyAnswer();
            if (answerState == AnswerState.ANSWER_EMPTY) {
                //This question has an empty answer (unanswered question).
                //Add its index to the list of unanswered questions.
                if (questionList.length() != 0) {
                    //Do not add a comma before the first element in the list.
                    questionList.append(",");
                }
                questionList.append(" ");
                questionList.append(i + 1);

                //Count this question as unanswered.
                ++emptyAnswerCount;
            } else if (answerState == AnswerState.ANSWER_WRONG) {
                ++wrongAnswerCount;
            }
        }

        AlertDialog ad = new AlertDialog.Builder(this).create();
        ad.setCancelable(false); // This blocks the 'BACK' button

        if (emptyAnswerCount != 0) {
            //There are unanswered questions, do not highlight anything and display a message
            //advising the user to fill all answers.
            if (emptyAnswerCount > 1) {
                //The number of empty answers is greater than 1, use the plural message for displaying
                //the list of unanswered questions.
                emptyAnswerMsg.append(questionList);
                Toast.makeText(getApplicationContext(), emptyAnswerMsg.toString(), Toast.LENGTH_LONG)
                        .show();
            } else {
                //The number of empty answers is 1, use the singular message for displaying
                //the list of unanswered questions.
                emptyAnswerMsgOne.append(questionList);
                Toast.makeText(getApplicationContext(), emptyAnswerMsgOne.toString(), Toast.LENGTH_LONG)
                        .show();
            }
        } else {
            //All the questions have been answered, correctly or incorrectly.

            //Highlight the correct and wrong answers.
            for (final AnswerChecker checker : answerCheckers) {
                AnswerState answerState = checker.GetCachedAnswerState();
                switch (answerState) {
                    case ANSWER_WRONG:
                        //This answer is wrong, highlight its correct answer.
                        checker.HighlightAsWrong(this);
                        break;
                    case ANSWER_CORRECT:
                        //This answer is correct, so display it as correct.
                        checker.HighlightAsCorrect(this);
                        break;
                }
            }

            //Display a summary of the final evaluation.
            if (wrongAnswerCount != 0) {
                //There are wrong answers, display the summary of correct vs total number of answers.
                //This message is displayed when the user completes the quiz, without any empty answers left.
                StringBuilder answerSummary = new StringBuilder(getString(R.string.answer_summary, name, NUM_QUESTIONS - wrongAnswerCount, NUM_QUESTIONS));

                final int SCORE_LOW = 6;
                final int SCORE_MEDIUM = 3;

                if (wrongAnswerCount >= SCORE_LOW) {
                    answerSummary.append(getString(R.string.message_score_low));
                } else if (wrongAnswerCount >= SCORE_MEDIUM) {
                    answerSummary.append(getString(R.string.message_score_medium));
                } else {
                    answerSummary.append(getString(R.string.message_score_high));
                }
                Toast.makeText(getApplicationContext(), answerSummary.toString(), Toast.LENGTH_LONG)
                        .show();

                ad.setMessage(answerSummary.toString());
                ad.setButton(BUTTON_POSITIVE,
                        "Review answers",
                        new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                final ScrollView scrollView = findViewById(R.id.scrollView);

                                scrollView.post(new Runnable() {
                                    public void run() {
                                        CardView questions = findViewById(R.id.card_question1);
                                        int q1 = questions.getTop();
                                        scrollView.scrollTo(0, q1 - 30);
                                    }
                                });
                            }


                        });
                ad.show();
            } else {
                //All the answers are correct, so congratulate the user with a message.
                ad.setMessage(getString(R.string.message_all_answers_correct, name));
                ad.setButton(BUTTON_POSITIVE,
                        "All answers are correct!",
                        new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                ad.show();
            }
        }
    }
}

