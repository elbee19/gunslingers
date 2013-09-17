package gunslinger.sim;

import java.security.AllPermission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

class StatsInfo
{
	int nplayers;
	int games;
	String[] playerNames;
	
	int[][] ranks;
	
	//Data structures for stats
	HashMap<Integer,Integer> streaks;
	int noOfRounds;
	ShotsStats[] playerShotStats;
	ShotsStats allShotStats;
//	int totalShotsFired;
	
	
	public StatsInfo(int nplayers, int games,String[] playerNames)
	{ 
		this.nplayers=nplayers;
		this.games=games;
		this.playerNames=playerNames;
		
		//-
		this.ranks=new int[nplayers][nplayers];
		streaks=new HashMap<Integer,Integer>();
		noOfRounds=0;
		playerShotStats=new ShotsStats[nplayers];
		for(int i=0;i<nplayers;i++)
		{
			playerShotStats[i]=new ShotsStats(playerNames[i]);
		}
		allShotStats=new ShotsStats("All Players");
//		totalShotsFired=0;
	}
	


	public void process(Gunslinger_Mod game) {
		// TODO Auto-generated method stub
		
		int[] rank=game.rank().clone();
		ArrayList<int[]> gameInfo=(ArrayList<int[]>) (game.gameInfo).clone();
		int[] gameRank=game.rank().clone();
		int[][] relationships=game.relationships.clone();
		
		try
		{
			processRank(gameRank);
			processHistory(gameInfo,relationships);
		}
		catch(Exception e)
		{
			System.out.println("Couldn't process this game");
			e.printStackTrace();
			System.out.println(e+"");
		}
		
	}
	
	public void write(String fname) {
		// TODO Auto-generated method stub
		
		System.out.println(fname);
		System.out.println(getRankStats());
		System.out.println(getStreaksStats());
		System.out.println(getAvgRoundLength());
		
		//Main Stats
		System.out.println(allShotStats.toString());
		for(int i=0;i<nplayers;i++)
			System.out.println(playerShotStats[i].toString());
		
	}
	
	public void processRank(int[] gameRank)
	{
		System.out.println(Arrays.toString(gameRank));
		
		for(int i=0;i<gameRank.length;i++)
		{
			ranks[i][gameRank[i]]++;
		}
		
		for(int[] a: ranks)
			System.out.println(Arrays.toString(a));

	}
	
	public void processHistory(ArrayList<int[]> gameInfo, int[][] relationships)
	{	
		int prevRound[]=null;
		int currRound[]=null;
		
		int streak[]=new int[nplayers];
		
		
		for(int round=0;round<gameInfo.size()-10;round++)
		{
			currRound=gameInfo.get(round);
			
			for(int shooter=0;shooter<nplayers;shooter++)
			{
				
				//Calculate streaks
				if(prevRound==null)
				{
					Arrays.fill(streak,1);
					break;
				}
				else
				{
					if(prevRound[shooter]==currRound[shooter])
					{
						streak[shooter]++;
					}
					else
					{
						if(!streaks.containsKey(streak[shooter]))
							streaks.put(streak[shooter], 0);
					
						streaks.put(streak[shooter], streaks.get(streak[shooter])+1);
						
						
						streak[shooter]=1;
					}
				}
				
				//Fill in the ShotsStats object
				processShot(shooter,currRound[shooter],relationships,prevRound,currRound);
			}
			
			prevRound=currRound;
		}	
		
		
		//No of rounds
		noOfRounds+=gameInfo.size();
		
	}
	
	public void processShot(int shooter, int target,int[][] relationships,int[] prevRound,int[] currRound)
	{
		assert currRound[shooter]==target;
		//playerShotStats;
		//allShotStats;
		
		if(target!=-1)
		{
			//Preprocessing
			boolean atFriend=relationships[shooter][target]==1;
			boolean atEnemy=relationships[shooter][target]==-1;
			boolean atNeutral=!atEnemy && !atFriend;
			boolean atWhoDidNotShoot=prevRound[target]==-1;
			
			boolean atWhoShotYou=prevRound[target]==shooter;
			
			boolean atWhoShotFriend=prevRound[target]!=-1?relationships[shooter][prevRound[target]]==1:false;
			
			boolean atWhoWasShot=false;
			boolean atWhoWasShotByFriend=false;
			for(int i=0;i<currRound.length;i++)
			{
				atWhoWasShot=atWhoWasShot || (prevRound[i]==target);
				if(relationships[shooter][i]==1)
					atWhoWasShotByFriend=atWhoWasShotByFriend || prevRound[i]==target;
			}
			
			//Processing
			allShotStats.totalShotsFired++;
			playerShotStats[shooter].totalShotsFired++;
			if(atEnemy)
			{
				allShotStats.atEnemy++;
				playerShotStats[shooter].atEnemy++;
				
				if(atWhoShotYou)
				{
					allShotStats.atWhoShotYou++;
					playerShotStats[shooter].atEnemyWhoShotYou++;
					

					System.out.println("blahh-"+allShotStats.atWhoShotYou);
					System.out.println("2blahh-"+playerShotStats[shooter].atEnemyWhoShotYou);
				}
				if(atWhoWasShot)
				{
					allShotStats.atWhoWasShot++;
					playerShotStats[shooter].atEnemyWhoWasShot++;
				}
				if(atWhoShotFriend)
				{
					allShotStats.atWhoShotFriend++;
					playerShotStats[shooter].atEnemyWhoShotFriend++;
				}
				if(atWhoWasShotByFriend)
				{
					allShotStats.atWhoWasShotByFriend++;
					playerShotStats[shooter].atEnemyWhoWasShotByFriend++;
				}
				if(atWhoDidNotShoot)
				{
					allShotStats.atWhoDidNotShoot++;
					playerShotStats[shooter].atEnemyWhoDidNotShoot++;
				}
			}
			else if(atFriend)
			{
				
				allShotStats.atFriend++;
				playerShotStats[shooter].atFriend++;
				
				if(atWhoShotYou)
				{
					allShotStats.atWhoShotYou++;
					playerShotStats[shooter].atFriendWhoShotYou++;
					
					System.out.println("blahh-"+allShotStats.atWhoShotYou);
					System.out.println("2blahh-"+playerShotStats[shooter].atFriendWhoShotYou);
				}
				if(atWhoWasShot)
				{
					allShotStats.atWhoWasShot++;
					playerShotStats[shooter].atFriendWhoWasShot++;
				}
				if(atWhoShotFriend)
				{
					allShotStats.atWhoShotFriend++;
					playerShotStats[shooter].atFriendWhoShotFriend++;
				}
				if(atWhoWasShotByFriend)
				{
					allShotStats.atWhoWasShotByFriend++;
					playerShotStats[shooter].atFriendWhoWasShotByFriend++;
				}
				if(atWhoDidNotShoot)
				{
					allShotStats.atWhoDidNotShoot++;
					playerShotStats[shooter].atFriendWhoDidNotShoot++;
				}
			}
			else if(atNeutral)
			{
				allShotStats.atNeutral++;
				playerShotStats[shooter].atNeutral++;
				
				
				if(atWhoShotYou)
				{
					allShotStats.atWhoShotYou++;
					
					
					playerShotStats[shooter].atNeutralWhoShotYou++;
					
					System.out.println("blahh-"+allShotStats.atWhoShotYou);
					System.out.println("2blahh-"+playerShotStats[shooter].atNeutralWhoShotYou);
				}
				if(atWhoWasShot)
				{
					allShotStats.atWhoWasShot++;
					playerShotStats[shooter].atNeutralWhoWasShot++;
				}
				if(atWhoShotFriend)
				{
					allShotStats.atWhoShotFriend++;
					playerShotStats[shooter].atNeutralWhoShotFriend++;
				}
				if(atWhoWasShotByFriend)
				{
					allShotStats.atWhoWasShotByFriend++;
					playerShotStats[shooter].atNeutralWhoWasShotByFriend++;
				}
				if(atWhoDidNotShoot)
				{
					allShotStats.atWhoDidNotShoot++;
					playerShotStats[shooter].atNeutralWhoDidNotShoot++;
				}
			}
			
			
		}
		else
		{
			allShotStats.noShotsFired++;
			playerShotStats[shooter].noShotsFired++;
			
			boolean noShotsWhenNotShotBefore=false;
			boolean noShotsWhenEnemyWasShot=false;
			boolean noShotsWhenFriendWasShot=false;
			boolean noShotsWhenFriendWasShooting=false;
			
			for(int i=0;i<currRound.length;i++)
			{
				if(prevRound[i]==-1)
					continue;
				
				noShotsWhenNotShotBefore=noShotsWhenNotShotBefore || shooter!=prevRound[i];
				noShotsWhenEnemyWasShot=noShotsWhenEnemyWasShot || relationships[shooter][prevRound[i]]==-1;
				noShotsWhenFriendWasShot=noShotsWhenFriendWasShot || relationships[shooter][prevRound[i]]==1;
				noShotsWhenNotShotBefore=noShotsWhenNotShotBefore || (relationships[shooter][i]==1 && prevRound[i]!=-1);
			}
			
			
			if(noShotsWhenNotShotBefore)
			{
				allShotStats.noShotsWhenNotShotBefore++;
				playerShotStats[shooter].noShotsWhenNotShotBefore++;
			}
			if(noShotsWhenEnemyWasShot)
			{
				allShotStats.noShotsWhenEnemyWasShot++;
				playerShotStats[shooter].noShotsWhenEnemyWasShot++;
			}
			if(noShotsWhenFriendWasShot)
			{
				allShotStats.noShotsWhenFriendWasShot++;
				playerShotStats[shooter].noShotsWhenFriendWasShot++;
			}
			if(noShotsWhenNotShotBefore)
			{
				allShotStats.noShotsWhenNotShotBefore++;
				playerShotStats[shooter].noShotsWhenNotShotBefore++;
			}
			
			
			
			
			
		}
	}
	
	
	public String getRankStats()
	{
		String retString="";
		
		for(int i=0;i<ranks.length;i++)
		{
			retString+=("P "+(i+1)+":");
			
			int total=0;
			for(int j=0;j<ranks.length;j++)
			{
				double perc= 100.0*ranks[i][j]/games;
				retString+=((j+1)+" - "+perc+"%, ");
			}
			retString+="\n";
		}
		
		return retString;
	}
	
	public String getStreaksStats()
	{
		String retString="Streaks distribution:\n";
		
		//Streaks
		for(Entry<Integer,Integer> e:streaks.entrySet())
		{
			retString+=e.getKey()+" "+e.getValue()+", ";
		}
		
		return retString.substring(0,retString.length()-2);
	}
	
	
	public String getAvgRoundLength()
	{
		return "Average no of rounds = "+(1.0*allShotStats.totalShotsFired/games);
	}
	
}


class ShotsStats
{
	public String name;
	
	public int totalShotsFired;
		public int atWhoShotYou;
		public int atWhoWasShot;
		public int atWhoShotFriend;
		public int atWhoWasShotByFriend;
		public int atWhoDidNotShoot;
		

		public int atEnemy;
			public int atEnemyWhoShotYou;
			public int atEnemyWhoWasShot;
			public int atEnemyWhoShotFriend;
			public int atEnemyWhoWasShotByFriend;
			public int atEnemyWhoDidNotShoot;
		
		public int atNeutral;
			public int atNeutralWhoShotYou;
			public int atNeutralWhoWasShot;
			public int atNeutralWhoShotFriend;
			public int atNeutralWhoWasShotByFriend;
			public int atNeutralWhoDidNotShoot;
			
		public int atFriend;
			public int atFriendWhoShotYou;
			public int atFriendWhoWasShot;
			public int atFriendWhoShotFriend;
			public int atFriendWhoWasShotByFriend;
			public int atFriendWhoDidNotShoot;
			
		public int noShotsFired;
			public int noShotsWhenNotShotBefore;
			public int noShotsWhenEnemyWasShot;
			public int noShotsWhenFriendWasShot;
			public int noShotsWhenFriendWasShooting;
	
	public ShotsStats(String name)
	{
		this.name=name;
		totalShotsFired=atWhoShotYou=atWhoWasShot=atWhoShotFriend=atWhoWasShotByFriend=atWhoDidNotShoot=atEnemy= atEnemyWhoShotYou= atEnemyWhoWasShot= atEnemyWhoShotFriend= atEnemyWhoWasShotByFriend= atEnemyWhoDidNotShoot=atNeutral= atNeutralWhoShotYou= atNeutralWhoWasShot= atNeutralWhoShotFriend= atNeutralWhoWasShotByFriend= atNeutralWhoDidNotShoot=atFriend= atFriendWhoShotYou= atFriendWhoWasShot= atFriendWhoShotFriend= atFriendWhoWasShotByFriend= atFriendWhoDidNotShoot= noShotsWhenNotShotBefore= noShotsWhenEnemyWasShot= noShotsWhenFriendWasShot= noShotsWhenFriendWasShooting=0;
	}
	
	public String toString()
	{
		
		String retString="";
		
		retString+=String.format("Stats for %s: %n", this.name);
		
		//General
		retString+=String.format("totalShotsFired= %d %n",totalShotsFired);
		if(totalShotsFired!=0)
		{
			retString+=String.format("atWhoShotYou= %d / %.2f %n", this.atWhoShotYou,100.0*atWhoShotYou/totalShotsFired);
			retString+=String.format("atWhoWasShot= %d / %.2f %n", this.atWhoWasShot,100.0*atWhoWasShot/totalShotsFired);
			retString+=String.format("atWhoShotFriend= %d / %.2f %n", this.atWhoShotFriend,100.0*atWhoShotFriend/totalShotsFired);
			retString+=String.format("atWhoWasShotByFriend= %d / %.2f %n", this.atWhoWasShotByFriend,100.0*atWhoWasShotByFriend/totalShotsFired);
			retString+=String.format("atWhoDidNotShoot= %d / %.2f %n", this.atWhoDidNotShoot,100.0*atWhoDidNotShoot/totalShotsFired);
			retString+="\n\n";
		}
		
		//Enemy

		retString+=String.format("atEnemy= %d %n",atEnemy);
		if(atEnemy!=0)
		{
			retString+=String.format("atEnemyWhoShotYou= %d / %.2f %n", this.atEnemyWhoShotYou,100.0*atEnemyWhoShotYou/atEnemy);
			retString+=String.format("atEnemyWhoWasShot= %d / %.2f %n", this.atEnemyWhoWasShot,100.0*atEnemyWhoWasShot/atEnemy);
			retString+=String.format("atEnemyWhoShotFriend= %d / %.2f %n", this.atEnemyWhoShotFriend,100.0*atEnemyWhoShotFriend/atEnemy);
			retString+=String.format("atEnemyWhoWasShotByFriend= %d / %.2f %n", this.atEnemyWhoWasShotByFriend,100.0*atEnemyWhoWasShotByFriend/atEnemy);
			retString+=String.format("atEnemyWhoDidNotShoot= %d / %.2f %n", this.atEnemyWhoDidNotShoot,100.0*atEnemyWhoDidNotShoot/atEnemy);
		}
		
		//Neutral

		retString+=String.format("atNeutral= %d %n",atNeutral);
		if(atNeutral!=0)
		{
			retString+=String.format("atNeutralWhoShotYou= %d / %.2f %n", this.atNeutralWhoShotYou,100.0*atNeutralWhoShotYou/atNeutral);
			retString+=String.format("atNeutralWhoWasShot= %d / %.2f %n", this.atNeutralWhoWasShot,100.0*atNeutralWhoWasShot/atNeutral);
			retString+=String.format("atNeutralWhoShotFriend= %d / %.2f %n", this.atNeutralWhoShotFriend,100.0*atNeutralWhoShotFriend/atNeutral);
			retString+=String.format("atNeutralWhoWasShotByFriend= %d / %.2f %n", this.atNeutralWhoWasShotByFriend,100.0*atNeutralWhoWasShotByFriend/atNeutral);
			retString+=String.format("atNeutralWhoDidNotShoot= %d / %.2f %n", this.atNeutralWhoDidNotShoot,100.0*atNeutralWhoDidNotShoot/atNeutral);
		}
		
		//Friend

		retString+=String.format("atFriend= %d %n",atFriend);
		if(atFriend!=0)
		{
			retString+=String.format("atFriendWhoShotYou= %d / %.2f %n", this.atFriendWhoShotYou,100.0*atFriendWhoShotYou/atFriend);
			retString+=String.format("atFriendWhoShotFriend= %d / %.2f %n", this.atFriendWhoShotFriend,100.0*atFriendWhoShotFriend/atFriend);
			retString+=String.format("atFriendWhoShotFriend= %d / %.2f %n", this.atFriendWhoShotFriend,100.0*atFriendWhoShotFriend/atFriend);
			retString+=String.format("atFriendWhoWasShotByFriend= %d / %.2f %n", this.atFriendWhoWasShotByFriend,100.0*atFriendWhoWasShotByFriend/atFriend);
			retString+=String.format("atFriendWhoDidNotShoot= %d / %.2f %n", this.atFriendWhoDidNotShoot,100.0*atFriendWhoDidNotShoot/atFriend);
		}
		//Noshots
		retString+=String.format("noShotsFired= %d %n",noShotsFired);
		retString+=String.format("noShotsWhenNotShotBefore= %d %n",noShotsWhenNotShotBefore);
		retString+=String.format("noShotsWhenEnemyWasShot= %d %n",noShotsWhenEnemyWasShot);
		retString+=String.format("noShotsWhenFriendWasShot= %d %n",noShotsWhenFriendWasShot);
		retString+=String.format("noShotsWhenFriendWasShooting= %d %n",noShotsWhenFriendWasShooting);
		
		
		
		return retString;
	}
}