package com.example.houcem.paperboardclienttry;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.io.StringBufferInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class PaperBoard extends AppCompatActivity implements Notifier,DrawNotifier  {

    private Socket          mSocket;
    private DataInputStream fluxEntree;
    private PrintStream     fluxSortie;
    private WhiteBoard board;
    private TextView mServerResponseTextView;
    private ConnectToServer newConnection;
    List<Point> points = new ArrayList<Point>();

    private String mServerResponse;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Resources res = getResources();
        setContentView(R.layout.activity_paper_board);
        mServerResponseTextView = ((TextView) findViewById(R.id.numberOfConnections));
        mServerResponseTextView.setText(mServerResponse);
        board = (WhiteBoard)this.findViewById(R.id.WhiteBoard);
        board.setBackgroundColor(Color.WHITE);
        board.requestFocus();
        board.register(this);
        newConnection = new ConnectToServer(this);
        newConnection.execute();
    }

    @Override
    public void TaskCompleted() {

        board.setPoints(points);
        board.invalidate();
        mServerResponseTextView.setText(mServerResponse);
    }

    @Override
    public void DrawComplete(List<Point> points) {

        if (fluxSortie!=null) {
            fluxSortie.print("AJOUT");
            for (int i = 0; i < points.size(); i++) {
                fluxSortie.print(String.valueOf(points.get(i).x)
                        + ' ' + points.get(i).y + ' ');
            }
            fluxSortie.write('\n');

        }
        board.invalidate();

    }

    public class ConnectToServer extends AsyncTask<Void,Void,Boolean> implements  Runnable{

        private Notifier delegate;

        public ConnectToServer(Notifier delegate) {

            this.delegate = delegate;
        }
        @Override
        protected Boolean doInBackground(Void... params) {

            try {

                    // Récupération de l'adresse de l'hôte www.eteks.com
                    InetAddress adresse = InetAddress.getByName("");  // inserer adresse ip du votre serveur
                    // Ouverture d'une connexion sur le port 26197 de cet hôte
                    mSocket = new Socket(adresse, 26197);

                    // Récupération des flux d'entrés et de sortie
                    fluxEntree = new DataInputStream(
                            new BufferedInputStream(mSocket.getInputStream()));
                    fluxSortie = new PrintStream(
                            new BufferedOutputStream(mSocket.getOutputStream()), true);

                    // Lancement d'un thread qui interroge à intervalle régulier
                    // la liste des polylines enregistrées sur le serveur
                    new Thread(this).start();

                }
                catch(IOException e)
                {
                    mServerResponse = "Probleme de connexion avec le serveur";
                }

            return true;
        }


        @Override
        public void run() {

            try
            {
                while (fluxSortie != null)
                {
                    // Envoi d'une requête CONNEXION pour récupérer le
                    // nombre de clients connectés au serveur
                    fluxSortie.print ("CONNEXION");
                    fluxSortie.write ('\n');

                    mServerResponse = fluxEntree.readLine () + " connexions";

                    // Envoi d'une requête LISTE pour récupérer
                    // la liste de toutes les polylines du serveur
                    fluxSortie.print ("LISTE");
                    fluxSortie.write ('\n');

                    String liste = fluxEntree.readLine ();

                    // Vidange de la liste pour la remettre à jour
                    points.clear();
                    StreamTokenizer tokens = new StreamTokenizer (
                            new StringBufferInputStream(liste));
                    tokens.parseNumbers ();
                    tokens.ordinaryChar ('\t');
                    tokens.whitespaceChars (' ', ' ');

                    // Décodage de la liste de points
                    while (tokens.nextToken () != StreamTokenizer.TT_EOF)
                    {

                        // Récupération des couples de valeurs (x,y)
                        // d'une polyline jusqu'à la prochaine tabulation
                        while (tokens.ttype != '\t')
                        {
                            Point point = new Point();
                            point.x = (int)tokens.nval;
                            tokens.nextToken ();
                            point.y = (int)tokens.nval;
                            tokens.nextToken ();
                            points.add(point);


                        }
                        // Ajout de la polyline à la liste

                    }


                    // Arrête le thread pendant 1 s avant de lancer
                    // une nouvelle demande de mise à jour
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            delegate.TaskCompleted();
                        }
                    });
                    Thread.sleep (1000);
                }
            }
            catch (InterruptedException e)
            { }
            catch (IOException e)
            { }

        }


        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    delegate.TaskCompleted();
                }
            });
        }

        public void stop ()
        {
            try
            {
                // Envoi d'une requête FIN et fermeture des flux
                fluxSortie.println ("FIN");
                fluxSortie.close ();
                fluxEntree.close ();
                mSocket.close ();
            }
            catch (IOException e)
            { }

            fluxSortie = null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    delegate.TaskCompleted();
                }
            });
        }

    }



}



