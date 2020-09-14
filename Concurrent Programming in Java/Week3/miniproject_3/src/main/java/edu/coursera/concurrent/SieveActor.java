package edu.coursera.concurrent;

import edu.rice.pcdp.Actor;
import static edu.rice.pcdp.PCDP.finish;

/**
 * An actor-based implementation of the Sieve of Eratosthenes.
 *
 * TODO Fill in the empty SieveActorActor actor class below and use it from
 * countPrimes to determin the number of primes <= limit.
 */
public final class SieveActor extends Sieve {
    /**
     * {@inheritDoc}
     *
     * TODO Use the SieveActorActor class to calculate the number of primes <=
     * limit in parallel. You might consider how you can model the Sieve of
     * Eratosthenes as a pipeline of actors, each corresponding to a single
     * prime number.
     */
    @Override
    public int countPrimes(final int limit) {
        // throw new UnsupportedOperationException();
        final SieveActorActor sieveActor = new SieveActorActor();
        finish(() -> {
            for(int i=3; i<=limit; i+=2) {
                sieveActor.send(i);
            }
            sieveActor.send(0);
        });

        int primeCount = 1;
        SieveActorActor curActor = sieveActor;
        while(curActor != null) {
            primeCount += curActor.numLocalPrimes;
            curActor = curActor.nextActor;
        }

        return primeCount;
    }

    /**
     * An actor class that helps implement the Sieve of Eratosthenes in
     * parallel.
     */
    public static final class SieveActorActor extends Actor {
        /**
         * Process a single message sent to this actor.
         *
         * TODO complete this method.
         *
         * @param msg Received message
         */

        int numLocalPrimes = 0;
        static final int MAX_LOCAL_PRIMES = 1000;
        int[] localPrimes = new int[MAX_LOCAL_PRIMES];

        SieveActorActor nextActor = null;

        boolean isLocalPrime(int number) {
            for(int i=0; i<numLocalPrimes; ++i) {
                if(number % localPrimes[i] == 0) return false;
            }
            return true;
        }

        @Override
        public void process(final Object msg) {
            // throw new UnsupportedOperationException();
            int message = (int) msg;

            if(message <= 0) {
                if(nextActor != null) {
                    nextActor.send(0);
                }
                return;
            }

            if(!isLocalPrime(message)) {
                return;
            }

            if(numLocalPrimes < MAX_LOCAL_PRIMES) {
                localPrimes[numLocalPrimes++] = message;
                return;
            }

            if(nextActor == null) {
                nextActor = new SieveActorActor();
            }

            nextActor.send(msg);
        }
    }
}
