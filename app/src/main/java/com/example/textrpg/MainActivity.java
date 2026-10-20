    private void performHeroAttack() {
        if (gameView != null) {
            gameView.triggerHit(); // Запуск визуальной анимации попадания
        }
        
        Random random = new Random();
        double baseDamage = statStrength;

        if (rageAttacksLeft > 0) {
            baseDamage *= (1.70 + (heroLevel / 5) * 0.10);
            rageAttacksLeft--;
        }

        int heroDamage = (int) Math.max(1, baseDamage * (0.8 + random.nextDouble() * 0.4));
        activeMonster.setCurrentHp(activeMonster.getCurrentHp() - heroDamage);

        if (activeMonster.getCurrentHp() <= 0) {
            processMonsterDefeat("Вы нанесли " + heroDamage + " урона.");
            return;
        }

        performMonsterAttack(heroDamage, "Вы нанесли " + heroDamage + " урона.\n");
    }
