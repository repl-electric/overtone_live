(ns d-_-b.core "_                 _
              _| |  ___     ___  | |_
             | . | |___|   |___| | . |
             |___|     _____      |___|
                      |_____|" (:use [overtone.live] [mud.core])(:require [mud.timing :as time][overtone.studio.fx :as fx][shadertone.tone :as t]))


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

(ctl-global-clock 8.0)
(pattern! note-buf (degrees-seq [:f#4 1 3 5 :f#3 1]))
(def pl (plucked :note-buf note-buf :beat-bus (:count time/beat-1th) :beat-trg-bus (:beat time/beat-1th)))
(ctl pl :attack 0.001 :sustain 0.001 :release 0.05)
(kill plucked)
