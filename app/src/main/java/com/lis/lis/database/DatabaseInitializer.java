package com.lis.lis.database;

import android.os.AsyncTask;
import com.lis.lis.database.daos.CardDao;
import com.lis.lis.database.daos.StackDao;
import com.lis.lis.database.entities.Card;
import com.lis.lis.database.entities.Stack;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class DatabaseInitializer {
    private static DatabaseInitializer databaseInitializer;
    private DatabaseInitializer(){}
    public static DatabaseInitializer getInstance() {
        if(databaseInitializer == null) {
            databaseInitializer = new DatabaseInitializer();
        }
        return databaseInitializer;
    }

    private static class GetAllStacks extends AsyncTask<Void, Void, List<String>> {

        private final StackDao mDao;

        private GetAllStacks(StackDao dao) { mDao = dao; }

        @Override
        protected List<String> doInBackground(Void... voids) {
            return mDao.getAll();
        }
    }
    public List<String> getAllStacks(StackDao dao) throws ExecutionException, InterruptedException {
        GetAllStacks task = new GetAllStacks(dao);
        return task.execute().get();
    }

    private static class GetCardsByStackName extends AsyncTask<String, Void, List<Card>> {

        private final CardDao mDao;

        private GetCardsByStackName(CardDao dao) { mDao = dao; }

        @Override
        protected List<Card> doInBackground(String... strings) {
            return mDao.getCardsByStackName(strings[0]);
        }
    }
    public List<Card> getCardsByStackName(CardDao dao, String stackName) throws ExecutionException, InterruptedException {
        GetCardsByStackName task = new GetCardsByStackName(dao);
        return task.execute(stackName).get();
    }

    private static class InsertCard extends AsyncTask<Card, Void, Void> {

        private final CardDao mDao;

        private InsertCard(CardDao dao) { mDao = dao; }

        @Override
        protected Void doInBackground(Card... cards) {
            mDao.insert(cards[0]);
            return null;
        }
    }
    public void insertCard(CardDao dao, Card card) {
        InsertCard task = new InsertCard(dao);
        task.execute(card);
    }

    private static class InsertStack extends AsyncTask<Stack, Void, Void> {

        private final StackDao mDao;

        private InsertStack(StackDao dao) { mDao = dao; }

        @Override
        protected Void doInBackground(Stack... stacks) {
            mDao.insert(stacks[0]);
            return null;
        }
    }
    public void insertStack(StackDao dao, Stack stack) {
        InsertStack task = new InsertStack(dao);
        task.execute(stack);
    }

    private static class DeleteStackByName extends AsyncTask<String, Void, Void> {

        final StackDao mDao;

        DeleteStackByName(StackDao dao) { mDao = dao; }

        @Override
        protected Void doInBackground(String... strings) {
            mDao.delete(strings[0]);
            return null;
        }
    }
    public void deleteStackByName(StackDao dao, String stackName) {
        DeleteStackByName task = new DeleteStackByName(dao);
        task.execute(stackName);
    }

    private static class GetStackByName extends AsyncTask<String, Void, Stack> {

        final StackDao mDao;

        GetStackByName(StackDao dao) { mDao = dao; }

        @Override
        protected Stack doInBackground(String... strings) {
            return mDao.getByName(strings[0]);
        }
    }
    public Stack getStackByName(StackDao dao, String stackName) throws ExecutionException, InterruptedException {
        GetStackByName task = new GetStackByName(dao);
        return task.execute(stackName).get();
    }

    private static class DeleteCardById extends AsyncTask<Integer, Void, Void> {

        final CardDao mDao;

        DeleteCardById(CardDao dao) { mDao = dao; }

        @Override
        protected Void doInBackground(Integer... integers) {
            mDao.deleteCardById(integers[0]);
            return null;
        }
    }
    public void deleteCardById(CardDao dao, int cardId) {
        DeleteCardById task = new DeleteCardById(dao);
        task.execute(cardId);
    }
}
