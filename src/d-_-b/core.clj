(ns d-_-b.core "_                 _
              _| |  ___     ___  | |_
             | . | |___|   |___| | . |
             |___|     _____     |___|
                      |_____|" (:use [overtone.live] [mud.core] [d-_-b.wavs])(:require [mud.timing :as time][overtone.studio.fx :as fx][shadertone.tone :as t]))
(do
  (defonce note-buf (buffer 256))
  (definst plucked [note-buf 0 beat-bus (:count time/beat-1th) beat-trg-bus (:beat time/beat-4th) note_slide 0 note_slide_shape 5 note_slide_curve 0 amp 1 amp_slide 0 amp_slide_shape 5
                    amp_slide_curve 0 pan 0 pan_slide 0 pan_slide_shape 5 pan_slide_curve 0 attack 0 decay 0 sustain 0
                    release 0.2 attack_level 1 sustain_level 1 env_curve 2 out_bus 0 cutoff 100 cutoff_slide 0
                    cutoff_slide_shape 5 cutoff_slide_curve 0 blip_rate 0.5 decay_time 0.9 decay_coef 0.4 room 200
                    reverb 8]
    (let [cnt       (in:kr beat-bus)
          trg       (in:kr beat-trg-bus)
          note      (buf-rd:kr 1 note-buf cnt)
          gate-trg (and (> note 0) trg)
          vol (set-reset-ff gate-trg)
          note      (varlag note note_slide note_slide_curve note_slide_shape)
          amp       (varlag amp amp_slide amp_slide_curve amp_slide_shape)
          pan       (varlag pan pan_slide pan_slide_curve pan_slide_shape)
          cutoff    (varlag cutoff cutoff_slide cutoff_slide_curve cutoff_slide_shape)
          cutoff-freq (midicps cutoff)
          freq (midicps note)
          amp-fudge 1
          src (sum [(sin-osc freq)
                    (saw freq)
                    (blip freq (* blip_rate (sin-osc:kr blip_rate)))])
          dly  (/ 1 freq)
          src (pluck src gate-trg dly dly decay_time (min decay_coef 0.99))
          src (rlpf src 1000)
          src (g-verb src (max room,1) reverb)
          src (lpf src cutoff-freq)
          env (env-gen:kr (env-adsr-ng attack decay sustain release attack_level sustain_level) :gate gate-trg)]
      (pan2 (* vol amp-fudge env src) pan amp)))


  (definst bass [note-buf 0 beat-bus (:count time/beat-1th) beat-trg-bus (:beat time/beat-4th) note_slide 0 note_slide_shape 5 note_slide_curve 0 amp 1 amp_slide 0 amp_slide_shape 5
                    amp_slide_curve 0 pan 0 pan_slide 0 pan_slide_shape 5 pan_slide_curve 0 attack 0 decay 0 sustain 0
                    release 0.2 attack_level 1 sustain_level 1 env_curve 2 out_bus 0 cutoff 100 cutoff_slide 0
                    cutoff_slide_shape 5 cutoff_slide_curve 0 blip_rate 0.5 decay_time 0.9 decay_coef 0.4 room 200
                    reverb 8]
    (let [cnt       (in:kr beat-bus)
          trg       (in:kr beat-trg-bus)
          note      (buf-rd:kr 1 note-buf cnt)
          gate-trg (and (> note 0) trg)
          vol (set-reset-ff gate-trg)
          note      (varlag note note_slide note_slide_curve note_slide_shape)
          amp       (varlag amp amp_slide amp_slide_curve amp_slide_shape)
          pan       (varlag pan pan_slide pan_slide_curve pan_slide_shape)
          cutoff    (varlag cutoff cutoff_slide cutoff_slide_curve cutoff_slide_shape)
          cutoff-freq (midicps cutoff)
          freq (midicps note)
          amp-fudge 1
          src (sum [(sin-osc freq)
                    (lf-saw freq)
                    (blip freq (* blip_rate (sin-osc:kr blip_rate)))])
          dly  (/ 1 freq)
          src (rlpf src 2000)
          src (g-verb src (max room,1) reverb)
          src (lpf src 200)
          env (env-gen:kr (env-adsr-ng attack decay sustain release attack_level sustain_level) :gate gate-trg)]
      (pan2 (* vol amp-fudge env src) pan amp)))



  (pattern! note-buf (map note [:C#4 :E3 0 0  :B3 :D4  0 :D4  :A4 :C#4, 0, :c#4]))
  (pattern! note-buf
            (repeat 8 (degrees-seq [:f#3 5 7 _ _]))
            (repeat 8 (degrees-seq [:f#3 4 6 _ 6]))
            (repeat 8 (degrees-seq [:f#3 3 5 _ 5]))
            (repeat 8 (degrees-seq [:f#3 2 3 _ 7]))
            (repeat 8 (degrees-seq [:f#3 1 3 _ 5]))
            (repeat 8 (degrees-seq [:f#3 1 3 6 5])))
  (defonce bass-buf (buffer 256))

  (pattern! bass-buf
            (repeat 8 (degrees-seq [:f#1 1 _ ]))
            (repeat 8 (degrees-seq [:f#1 6 _ ]))
            (repeat 8 (degrees-seq [:f#1 5 _ ]))
            (repeat 8 (degrees-seq [:f#1 2 _ ]))
            (repeat 8 (degrees-seq [:f#1 3 _ ]))
            (repeat 8 (degrees-seq [:f#1 5 _ ])))

  (defonce bass-buf2 (buffer 256))

  (ctl-global-clock 8.0)



(do (defonce kick-seq-buf (buffer 256)) (defonce bass-notes-buf (buffer 256)) (defonce drums-g (group "main drums")) (defonce kick-amp (buffer 256))
    (def kicker (space-kick2 [:head drums-g] :note-buf bass-notes-buf :seq-buf kick-seq-buf :num-steps 16 :beat-num 0 :noise 0.05 :amp 0.0 :mod-index 0.1 :mod-freq 4.0 :mode-freq 0.2))(ctl kicker :amp-buf kick-amp)
    (pattern! kick-amp  [2.0])
    (pattern! bass-notes-buf (degrees [1 1 1 1 1 1 1 1] :minor :F2))
    )
;;(kill space-kick2)


(do (defonce drums-g (group "drums")) (defonce drum-effects-g (group "drums effects for extra sweetness")) (defbufs 128 [hats-buf white-seq-buf]) (defonce hats-amp (buffer 256)) (pattern! hats-amp  (repeat 3 [2 2 2 2 2.1 2 2 2   2 2 2 2 2 2 2 2]) [2 2 2 2 2.1 2 2 2   2 2 2.4 2 2.4 2 2 2])(def white (whitenoise-hat [:head drums-g] :amp-buf hats-amp :seq-buf hats-buf :beat-bus (:count time/beat-1th) :beat-trg-bus (:beat time/beat-1th) :num-steps 16 :release 0.1 :attack 0.0 :beat-num 0)) (ctl white :amp-buf hats-amp))
(ctl white :attack 0.04 :release 0.01 :amp 1)
;;(ctl white :attack 0.002 :release 0.04 :amp 1.5)

(pattern! hats-buf     [0 0 0 0     1 0 0 0])
(pattern! kick-seq-buf [1 0 0 0     0 0 0 0
                        0 0 0 0     0 0 0 0
                        0 0 0 0     0 0 0 0
                        0 0 0 0     0 0 0 0
                        ])

(def drum-sample (sample "/Users/josephwilk/Workspace/music/samples/Mountain/One Shots/Kick/SubKick_01_SP.wav"))
(def snare-sample (sample "/Users/josephwilk/Workspace/music/samples/MagicDust/ORGANIC_HIT_HI/MD_ORGANIC_HIT_HI_057.wav" ))
(def snare-sample (sample "/Users/josephwilk/Workspace/music/samples/ModTech/ss_mt_sound and fx/ss_mt_drum hits/SS_MT_SNARE/SS_MT_SNARE_002.wav" ))



(do(defonce drum-effects-g (group "drum effects"))(def kick-sample drum-sample)(defonce effects2-seq-buf (buffer 256))(defonce effects-seq-buf (buffer 256))
   (pattern! effects-seq-buf  (repeat 28 [1 0 0 0  0 0 0 0]))
   (pattern! effects2-seq-buf (repeat 28 [0 0 1 1  0 0 0 0]))
   (def snare-organ-seq1 (efficient-seqer [:head drum-effects-g] :buf kick-sample :pattern effects-seq-buf :rate-start 1.0 :rate-limit 1.0 :amp 0.03))
   (def snare-organ-seq2 (efficient-seqer [:head drum-effects-g] :buf kick-sample :pattern effects2-seq-buf :rate-start 0.95 :rate-limit 0.8 :amp 0.02)))


(do(defonce drum-effects-g (group "drum effects"))(def snare-sample snare-sample)(defonce effects3-seq-buf (buffer 256))(defonce effects4-seq-buf (buffer 256))
   (pattern! effects3-seq-buf   [0 0 0 0 1 0 0 0    0 0 0 0 0 0 0 0])
   (pattern! effects4-seq-buf   [0 0 0 0 0 0 0 0    0 0 0 0 1 0 0 0])
   (def snare-organ-seq3 (efficient-seqer [:head drum-effects-g] :buf snare-sample :pattern effects3-seq-buf :rate-start 1.0 :rate-limit 1.0 :amp 0.02))
   (def snare-organ-seq4 (efficient-seqer [:head drum-effects-g] :buf snare-sample :pattern effects4-seq-buf :rate-start 0.95 :rate-limit 0.8 :amp 0.01)))


(ctl drum-effects-g :amp 1.0)
(ctl snare-organ-seq3 :amp 0.03)
(ctl snare-organ-seq4 :amp 0.01)

(kill efficient-seqer)
(ctl kicker :attack 0.00001 :sustain 0.125 :amp 2.0)

  (def pl  (plucked :note-buf note-buf :beat-bus (:count time/beat-2th) :beat-trg-bus (:beat time/beat-2th)))
  (def pl2 (bass :note-buf bass-buf :beat-bus (:count time/beat-4th) :beat-trg-bus (:beat time/beat-4th)))
  (def pl3 (plucked :note-buf bass-buf2 :beat-bus (:count time/beat-1th) :beat-trg-bus (:beat time/beat-1th)))

  (pattern! bass-buf2
            (repeat 8 (degrees-seq [:f#4 1 1 _ 1 ]))
            (degrees-seq [:f#4 1 1 _ :f#3 4 ])
            (repeat 8 (degrees-seq [:f#4 1 1 _ 2 ]))
            (degrees-seq [:f#4 4 4 _ :f#4 1 ])
            )

  (ctl pl3 :attack 0.5 :sustain 0.25 :release 0.25 :amp 0.1)
  (ctl pl2 :attack 0.0125 :sustain 0.5   :release 0.5 :amp 0.15)
  (ctl pl  :attack 0.01   :sustain 0.5   :release 0.5 :amp 0.2)

  ;; (kill plucked)
  ;; (kill bass)

  )
